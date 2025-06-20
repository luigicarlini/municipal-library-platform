package it.comune.library.reservation.controller;

import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.dto.BookDto;
import it.comune.library.reservation.mapper.BookMapper;
import it.comune.library.reservation.repository.BookRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.transaction.annotation.Propagation;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import it.comune.library.reservation.config.RestExceptionAdvice.ApiError;

/**
 * 📚 REST controller per la gestione dei libri.
 */
@RestController
@RequestMapping("/books")
@Tag(name = "BookController", description = "Gestione dei libri")
public class BookController {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookController(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    /* ---------- SEARCH avanzata ---------- */

    @Operation(summary = "Ricerca avanzata con filtri opzionali")
    @ApiResponse(responseCode = "200", description = "Lista di libri")
    @GetMapping
    public ResponseEntity<List<BookDto>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer publicationYear) {

        var books = bookRepository.searchByOptionalFilters(
                title, author, genre, isbn, publicationYear);

        var dto = books.stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    /* ---------- READ singolo ---------- */

    @Operation(summary = "Recupera un libro per ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro trovato"),
            @ApiResponse(responseCode = "404", description = "Non trovato")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable UUID id) {
        return bookRepository.findById(id)
                .map(bookMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* ---------- UPDATE ---------- */
    @Operation(summary = "Aggiorna un libro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aggiornato"),
            @ApiResponse(responseCode = "404", description = "Non trovato"),
            @ApiResponse(responseCode = "409", description = "Versione non corrente")
    })
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<BookDto> updateBook(@PathVariable UUID id,
            @RequestBody BookDto dto) {

        return bookRepository.findById(id)
                .map(existing -> {
                    // ⚠️ Controllo OCC manuale: version mismatch
                    Integer currentVersion = existing.getVersion();
                    Integer incomingVersion = dto.getVersion();

                    if (!Objects.equals(currentVersion, incomingVersion)) {
                        throw new OptimisticLockException(
                                "Versione non aggiornata. Versione attuale = " + currentVersion +
                                        ", richiesta = " + incomingVersion);
                    }

                    // ✅ Applica solo i campi modificabili su entità gestita
                    bookMapper.updateEntity(existing, dto);

                    // ✅ Il salvataggio è sicuro, Hibernate monitora l'entità "managed"
                    Book saved = bookRepository.save(existing);

                    return ResponseEntity.ok(bookMapper.toDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /* ---------- DELETE soft | hard ---------- */
    @Operation(summary = "Elimina un libro (soft di default, hard con mode=hard)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminato"),
            @ApiResponse(responseCode = "404", description = "Non trovato")
    })
    @DeleteMapping("/{id}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id,
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

    @Operation(summary = "Crea un nuovo libro")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Libro creato con successo"),
            @ApiResponse(responseCode = "409", description = "ISBN duplicato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
@PostMapping
public ResponseEntity<?> createBook(
        @Valid @RequestBody BookDto dto,
        UriComponentsBuilder uriBuilder) {

    /* 1) ISBN già usato da un libro NON cancellato → 409 */
    if (bookRepository.existsByIsbnAndDeletedFalse(dto.getIsbn())) {
        return conflict("ISBN duplicato");
    }

    /* 2) mappa DTO → entity */
    Book entity = bookMapper.toEntity(dto);

    try {
        /* save + flush immediato per far emergere subito eventuali vincoli */
        Book saved = bookRepository.saveAndFlush(entity);

        /* 3) Location header */
        URI location = uriBuilder
                .path("/books/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity              // 201 + Location + body
                .created(location)
                .body(bookMapper.toDto(saved));

    } catch (DataIntegrityViolationException ex) {
        /* corner-case: stessa ISBN inserita da un’altra transazione
           tra il check (#1) e il flush */
        return conflict("ISBN duplicato");
    }
}

/* ───── helpers ───── */
private static ResponseEntity<ApiError> conflict(String msg) {
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ApiError(HttpStatus.CONFLICT.value(), msg));
}
}
