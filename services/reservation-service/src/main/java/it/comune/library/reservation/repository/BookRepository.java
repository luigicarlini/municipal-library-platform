package it.comune.library.reservation.repository;

import it.comune.library.reservation.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repository per l'entità {@link Book}, consente l'accesso ai dati tramite JPA.
 */
public interface BookRepository extends JpaRepository<Book, UUID> {

    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    List<Book> findByGenreContainingIgnoreCase(String genre);
    List<Book> findByIsbn(String isbn);

    /**
     * 🔍 Ricerca con filtri combinabili opzionali.
     *
     * @param title titolo (substring, case-insensitive)
     * @param author autore (substring, case-insensitive)
     * @param genre genere (substring, case-insensitive)
     * @param isbn codice ISBN (match esatto)
     * @param publicationYear anno pubblicazione (match esatto)
     * @return lista filtrata
     */
    @Query("""
    SELECT b FROM Book b
    WHERE (:title IS NULL OR b.title ILIKE CONCAT('%', CAST(:title AS string), '%'))
      AND (:author IS NULL OR b.author ILIKE CONCAT('%', CAST(:author AS string), '%'))
      AND (:genre IS NULL OR b.genre ILIKE CONCAT('%', CAST(:genre AS string), '%'))
      AND (:isbn IS NULL OR b.isbn = :isbn)
      AND (:publicationYear IS NULL OR b.publicationYear = :publicationYear)
""")
List<Book> searchByOptionalFilters(
        @Param("title") String title,
        @Param("author") String author,
        @Param("genre") String genre,
        @Param("isbn") String isbn,
        @Param("publicationYear") Integer publicationYear
);

}
