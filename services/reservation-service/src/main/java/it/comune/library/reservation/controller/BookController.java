package it.comune.library.reservation.controller;

import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.dto.BookDto;
import it.comune.library.reservation.mapper.BookMapper;
import it.comune.library.reservation.repository.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Propagation;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        var dto = books.stream().map(bookMapper::toDto).collect(Collectors.toList());
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
                    bookMapper.updateEntity(existing, dto); // mutate entity
                    var saved = bookRepository.save(existing); // flush
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
    @Transactional(propagation = Propagation.REQUIRES_NEW) // commit immediato
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id,
            @RequestParam(defaultValue = "soft") String mode) {

        /* HARD-DELETE: rimozione fisica, bypassa @Where --------------------- */
        if ("hard".equalsIgnoreCase(mode)) {
            int rows = bookRepository.hardDeleteByIdNative(id);
            return rows > 0
                    ? ResponseEntity.noContent().<Void>build()
                    : ResponseEntity.notFound().<Void>build();
        }

        /* SOFT-DELETE: flag deleted = true ---------------------------------- */
        return bookRepository.findById(id) // filtrato (deleted = false)
                .map(book -> {
                    book.setDeleted(true); // dirty-checking
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().<Void>build());
    }

    /* ---------- CREATE ---------- */

    @Operation(summary = "Crea un nuovo libro")
    @ApiResponse(responseCode = "201", description = "Creato")
    @PostMapping
    public ResponseEntity<BookDto> createBook(@RequestBody BookDto dto) {
        Book saved = bookRepository.save(bookMapper.toEntity(dto));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookMapper.toDto(saved));
    }
}
