package it.comune.library.reservation.controller;

import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.dto.BookDto;
import it.comune.library.reservation.mapper.BookMapper;
import it.comune.library.reservation.repository.BookRepository;
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
public class BookController {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookController(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    /**
     * 🔍 Recupera tutti i libri o esegue una ricerca con filtri combinabili.
     *
     * @param title titolo del libro (opzionale, case-insensitive)
     * @param author autore del libro (opzionale, case-insensitive)
     * @param genre genere del libro (opzionale, case-insensitive)
     * @param isbn codice ISBN (opzionale)
     * @param publicationYear anno di pubblicazione (opzionale)
     * @return lista dei libri filtrati o completa se nessun filtro è passato
     */
    @GetMapping
    public ResponseEntity<List<BookDto>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer publicationYear
    ) {
/*      -- only for debugging ---
        System.out.printf("DEBUG: title=%s (%s), author=%s (%s), genre=%s (%s), isbn=%s (%s), pubYear=%s (%s)%n",
        title, title != null ? title.getClass().getName() : "null",
        author, author != null ? author.getClass().getName() : "null",
        genre, genre != null ? genre.getClass().getName() : "null",
        isbn, isbn != null ? isbn.getClass().getName() : "null",
        publicationYear, publicationYear != null ? publicationYear.getClass().getName() : "null"); */

        List<Book> books = bookRepository.searchByOptionalFilters(title, author, genre, isbn, publicationYear);
        List<BookDto> dtos = books.stream().map(bookMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * 🔍 Recupera un libro tramite ID.
     *
     * @param id UUID del libro
     * @return libro corrispondente o 404 se non trovato
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable UUID id) {
        Optional<Book> book = bookRepository.findById(id);
        return book.map(value -> ResponseEntity.ok(bookMapper.toDto(value)))
                   .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 🔍 Ricerca libri per titolo (case-insensitive, substring match).
     */
    @GetMapping("/search/title")
    public ResponseEntity<List<BookDto>> searchByTitle(@RequestParam String title) {
        List<Book> results = bookRepository.findByTitleContainingIgnoreCase(title);
        return ResponseEntity.ok(results.stream().map(bookMapper::toDto).collect(Collectors.toList()));
    }

    /**
     * 🔍 Ricerca libri per autore (case-insensitive, substring match).
     */
    @GetMapping("/search/author")
    public ResponseEntity<List<BookDto>> searchByAuthor(@RequestParam String author) {
        List<Book> results = bookRepository.findByAuthorContainingIgnoreCase(author);
        return ResponseEntity.ok(results.stream().map(bookMapper::toDto).collect(Collectors.toList()));
    }

    /**
     * 🔍 Ricerca libri per genere (case-insensitive, substring match).
     */
    @GetMapping("/search/genre")
    public ResponseEntity<List<BookDto>> searchByGenre(@RequestParam String genre) {
        List<Book> results = bookRepository.findByGenreContainingIgnoreCase(genre);
        return ResponseEntity.ok(results.stream().map(bookMapper::toDto).collect(Collectors.toList()));
    }
}
