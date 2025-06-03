package it.comune.library.reservation.controller;

import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.dto.BookDto;
import it.comune.library.reservation.mapper.BookMapper;
import it.comune.library.reservation.repository.BookRepository;

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

    /**
     * 🔍 Aggiorna un libro esistente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(@PathVariable UUID id, @RequestBody BookDto bookDto) {
        // Verifica se il libro con l'ID specificato esiste nel database
        Optional<Book> existingBook = bookRepository.findById(id);
        if (existingBook.isEmpty()) {
            // Se non esiste, restituisce HTTP 404 Not Found
            return ResponseEntity.notFound().build();
        }

        // Converte il DTO in un'entità Book
        Book bookToUpdate = bookMapper.toEntity(bookDto);

        // Imposta l'ID dell'entità al valore specificato nel path (evita la creazione
        // di un nuovo record)
        bookToUpdate.setId(id);

        // Salva l'entità aggiornata nel database (effettua l'UPDATE)
        Book updatedBook = bookRepository.save(bookToUpdate);

        // Converte l'entità aggiornata in DTO e restituisce HTTP 200 OK con il corpo
        // aggiornato
        return ResponseEntity.ok(bookMapper.toDto(updatedBook));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        /*
         * Endpoint HTTP DELETE per eliminare un libro esistente identificato dal suo
         * UUID.
         *
         * - Path: /books/{id}
         * - Metodo: DELETE
         * - Parametri: UUID id (nella path)
         * - Comportamento:
         * - Verifica se esiste un libro con l'id fornito.
         * - Se esiste, lo elimina dal database e restituisce HTTP 204 No Content.
         * - Se non esiste, restituisce HTTP 404 Not Found.
         */
        Optional<Book> optionalBook = bookRepository.findById(id);

        if (optionalBook.isEmpty()) {
            // Libro non trovato → 404 Not Found
            return ResponseEntity.notFound().build();
        }

        // Libro trovato → lo eliminiamo
        bookRepository.deleteById(id);

        // Restituiamo 204 No Content per indicare che l'operazione è andata a buon fine
        // ma senza corpo di risposta
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint per la creazione di un nuovo libro.
     * Accetta un oggetto BookDto nel body della richiesta e lo salva nel database.
     *
     * @param bookDto DTO con i dati del libro da creare
     * @return BookDto rappresentante il libro salvato con ID generato
     */
    @PostMapping
    public ResponseEntity<BookDto> createBook(@RequestBody BookDto bookDto) {
        // Conversione da DTO a entità
        Book book = bookMapper.toEntity(bookDto);

        // Salvataggio nel database tramite il repository
        Book savedBook = bookRepository.save(book);

        // Conversione dell'entità salvata in DTO per la risposta
        BookDto responseDto = bookMapper.toDto(savedBook);

        // Ritorna il libro creato con codice HTTP 201 (Created)
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }



}
