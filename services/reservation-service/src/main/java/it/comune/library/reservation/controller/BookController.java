package it.comune.library.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.dto.BookDto;
import it.comune.library.reservation.mapper.BookMapper;
import it.comune.library.reservation.repository.BookRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller per l’entità {@link Book}.
 */
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "book-entity-controller", description = "Gestione dei libri (CRUD, ricerca, soft/hard-delete)")
public class BookController {

    private final BookRepository bookRepository;
    private final BookMapper     bookMapper;

    /* ─────────────────────────────────────
     * 1. SEARCH  (filtri + paging + sort)
     * ──────────────────────────────────── */
    @Operation(
        summary     = "Ricerca libri con filtri, paginazione e ordinamento",
        description = """
            <p>Filtri facoltativi:</p>
            <ul>
              <li><code>title</code>, <code>author</code>, <code>genre</code> &nbsp;→&nbsp; <em>contains&nbsp; &nbsp;(case-insensitive)</em></li>
              <li><code>isbn</code> &nbsp;→&nbsp; match esatto</li>
              <li><code>publicationYear</code> &nbsp;→&nbsp; anno intero</li>
            </ul>
            <p>Paging: <code>page</code> (0-based) + <code>size</code>.<br>
               Ordinamento: <code>sort</code> (campo) + <code>dir</code> (ASC | DESC).</p>
            <p>L’header <code>X-Total-Count</code> restituisce il totale risultati.</p>
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200",
                     description   = "Pagina di risultati",
                     content       = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<List<BookDto>> searchBooks(
            /* filtri --------------------------------------------------- */
            @Parameter(example = "Il nome della rosa")
            @RequestParam(required = false) String  title,
            @Parameter(example = "Eco")
            @RequestParam(required = false) String  author,
            @Parameter(example = "Romanzo storico")
            @RequestParam(required = false) String  genre,
            @Parameter(example = "9788807888612")
            @RequestParam(required = false) String  isbn,
            @Parameter(example = "1980")
            @RequestParam(required = false) Integer publicationYear,
            /* paging + sort ------------------------------------------- */
            @Parameter(description = "Indice pagina (0-based)", example = "0")
            @RequestParam(defaultValue = "0")  int page,
            @Parameter(description = "Dimensione pagina", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo ordinamento", example = "title")
            @RequestParam(defaultValue = "title") String sort,
            @Parameter(description = "Direzione ordinamento",
                       schema      = @Schema(allowableValues = {"ASC","DESC"}))
            @RequestParam(defaultValue = "ASC")  Sort.Direction dir) {

        Pageable pageable   = PageRequest.of(page, size, Sort.by(dir, sort));
        Page<Book> pageData = bookRepository.searchByOptionalFilters(
                title, author, genre, isbn, publicationYear, pageable);

        List<BookDto> body = pageData.getContent()
                                     .stream()
                                     .map(bookMapper::toDto)
                                     .collect(Collectors.toList());

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(pageData.getTotalElements()))
                .body(body);
    }

    /* ─────────────────────────────────────
     * 2. READ singolo
     * ──────────────────────────────────── */
    @Operation(summary = "Recupera un libro tramite ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Libro trovato"),
        @ApiResponse(responseCode = "404", description = "Libro non esistente")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBook(@PathVariable UUID id) {
        return bookRepository.findById(id)
                .map(bookMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* ─────────────────────────────────────
     * 3. CREATE
     * ──────────────────────────────────── */
    @Operation(
        summary     = "Crea un nuovo libro",
        description = "Richiede un JSON conforme a <code>BookDto</code>. L’ISBN dev’essere unico fra i libri non eliminati."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Libro creato (Location header)"),
        @ApiResponse(responseCode = "409", description = "ISBN duplicato"),
        @ApiResponse(responseCode = "422", description = "Dati non validi")
    })
    @PostMapping
    public ResponseEntity<?> createBook(
            @Valid @RequestBody BookDto dto,
            UriComponentsBuilder      uri) {

        if (bookRepository.existsByIsbnAndDeletedFalse(dto.getIsbn())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body("ISBN duplicato");
        }

        try {
            Book saved = bookRepository.saveAndFlush(bookMapper.toEntity(dto));

            URI location = uri.path("/books/{id}")
                              .buildAndExpand(saved.getId())
                              .toUri();

            return ResponseEntity.created(location)
                                 .body(bookMapper.toDto(saved));

        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body("ISBN duplicato");
        }
    }

    /* ─────────────────────────────────────
     * 4. UPDATE
     * ──────────────────────────────────── */
    @Operation(
        summary     = "Aggiorna un libro esistente",
        description = "Richiede il campo <code>version</code> corrente per garantire l’optimistic locking."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Aggiornato"),
        @ApiResponse(responseCode = "404", description = "Libro non trovato"),
        @ApiResponse(responseCode = "409", description = "Versione non corrente")
    })
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<BookDto> updateBook(
            @PathVariable UUID id,
            @Valid @RequestBody BookDto dto) {

        return bookRepository.findById(id)
                .map(entity -> {
                    if (!Objects.equals(entity.getVersion(), dto.getVersion())) {
                        throw new OptimisticLockException(
                                "Stale version. Current=" + entity.getVersion()
                                + ", incoming=" + dto.getVersion());
                    }

                    bookMapper.updateEntity(entity, dto);
                    Book saved = bookRepository.saveAndFlush(entity);   // <-- flush come in origine
                    return ResponseEntity.ok(bookMapper.toDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /* ─────────────────────────────────────
     * 5. DELETE (soft di default)
     * ──────────────────────────────────── */
    @Operation(
        summary     = "Elimina un libro",
        description = """
            <ul>
              <li><strong>Soft-delete</strong> – imposta <code>deleted=true</code>.</li>
              <li><strong>Hard-delete</strong> – passare <code>mode=hard</code> per rimozione fisica.</li>
            </ul>"""
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Eliminato"),
        @ApiResponse(responseCode = "404", description = "Libro non trovato")
    })
    @DeleteMapping("/{id}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Void> deleteBook(
            @PathVariable UUID id,
            @Parameter(description = "soft | hard", example = "soft")
            @RequestParam(defaultValue = "soft") String mode) {

        if ("hard".equalsIgnoreCase(mode)) {
            int rows = bookRepository.hardDeleteByIdNative(id);
            return rows > 0
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        }

        return bookRepository.findById(id)
                .map(book -> {
                    book.setDeleted(true);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().<Void>build());
    }
}