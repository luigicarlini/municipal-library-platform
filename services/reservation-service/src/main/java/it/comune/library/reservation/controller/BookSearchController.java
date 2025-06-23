package it.comune.library.reservation.controller;

import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/books", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class BookSearchController {

    private final BookRepository repo;

    /**
     * GET /books?[title=&author=&genre=&isbn=&publicationYear=]
     *
     * Se arriva almeno uno dei parametri, il metodo viene eseguito
     * (altrimenti lascia il controllo a Spring-Data-REST).
     */
    @GetMapping(params = {
            "title",          // almeno uno dei parametri ⇒ mapping valido
            "author",
            "genre",
            "isbn",
            "publicationYear"
    })
    public List<Book> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer publicationYear
    ) {
        return repo.searchByOptionalFilters(title, author, genre, isbn, publicationYear);
    }
}

