package it.comune.library.reservation.controller;

import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.dto.BookDto;
import it.comune.library.reservation.mapper.BookMapper;
import it.comune.library.reservation.repository.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 📚 REST controller per la gestione delle risorse Book.
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

    @Operation(summary = "Ricerca avanzata dei libri con filtri opzionali")
    @ApiResponse(responseCode = "200", description = "Lista di libri restituita con successo")
    @GetMapping
    public ResponseEntity<List<BookDto>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer publicationYear
    ) {
        List<Book> books = bookRepository.searchByOptionalFilters(title, author, genre, isbn, publicationYear);
        List<BookDto> dtos = books.stream().map(bookMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Recupera un libro tramite ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro trovato"),
            @ApiResponse(responseCode = "404", description = "Libro non trovato")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable UUID id) {
        Optional<Book> book = bookRepository.findById(id);
        return book.map(value -> ResponseEntity.ok(bookMapper.toDto(value)))
                   .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Ricerca libri per titolo (substring match, case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Libri trovati")
    @GetMapping("/search/title")
    public ResponseEntity<List<BookDto>> searchByTitle(@RequestParam String title) {
        List<Book> results = bookRepository.findByTitleContainingIgnoreCase(title);
        return ResponseEntity.ok(results.stream().map(bookMapper::toDto).collect(Collectors.toList()));
    }

    @Operation(summary = "Ricerca libri per autore (substring match, case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Libri trovati")
    @GetMapping("/search/author")
    public ResponseEntity<List<BookDto>> searchByAuthor(@RequestParam String author) {
        List<Book> results = bookRepository.findByAuthorContainingIgnoreCase(author);
        return ResponseEntity.ok(results.stream().map(bookMapper::toDto).collect(Collectors.toList()));
    }

    @Operation(summary = "Ricerca libri per genere (substring match, case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Libri trovati")
    @GetMapping("/search/genre")
    public ResponseEntity<List<BookDto>> searchByGenre(@RequestParam String genre) {
        List<Book> results = bookRepository.findByGenreContainingIgnoreCase(genre);
        return ResponseEntity.ok(results.stream().map(bookMapper::toDto).collect(Collectors.toList()));
    }

    @Operation(summary = "Aggiorna un libro esistente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro aggiornato con successo"),
            @ApiResponse(responseCode = "404", description = "Libro non trovato")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(@PathVariable UUID id, @RequestBody BookDto bookDto) {
        Optional<Book> existingBook = bookRepository.findById(id);
        if (existingBook.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Book bookToUpdate = bookMapper.toEntity(bookDto);
        bookToUpdate.setId(id);
        Book updatedBook = bookRepository.save(bookToUpdate);
        return ResponseEntity.ok(bookMapper.toDto(updatedBook));
    }

    @Operation(summary = "Elimina un libro tramite ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Libro eliminato con successo"),
            @ApiResponse(responseCode = "404", description = "Libro non trovato")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        Optional<Book> optionalBook = bookRepository.findById(id);

        if (optionalBook.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        bookRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Crea un nuovo libro")
    @ApiResponse(responseCode = "201", description = "Libro creato con successo")
    @PostMapping
    public ResponseEntity<BookDto> createBook(@RequestBody BookDto bookDto) {
        Book book = bookMapper.toEntity(bookDto);
        Book savedBook = bookRepository.save(book);
        BookDto responseDto = bookMapper.toDto(savedBook);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
