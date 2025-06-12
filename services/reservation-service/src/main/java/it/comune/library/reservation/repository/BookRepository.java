package it.comune.library.reservation.repository;

import it.comune.library.reservation.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.UUID;

/**
 * Repository per l'entità {@link Book}, accesso dati tramite JPA.
 *
 * Gli alias @RestResource(path="…") garantiscono la retro-compatibilità
 * con i vecchi endpoint: /books/search/find-by-*
 */
@RepositoryRestResource(path = "books", collectionResourceRel = "books")
public interface BookRepository extends JpaRepository<Book, UUID> {

    /*
     * ────────────────────────────────────────────────────────────── *
     * ALIAS «LEGACY» – riconducono ai metodi già esistenti, *
     * nessuna logica duplicata, cambia soltanto il path esposto. *
     * ──────────────────────────────────────────────────────────────
     */

    @RestResource(path = "find-by-title", rel = "find-by-title")
    List<Book> findByTitleContainingIgnoreCase(@Param("title") String title);

    @RestResource(path = "find-by-author", rel = "find-by-author")
    List<Book> findByAuthorContainingIgnoreCase(@Param("author") String author);

    @RestResource(path = "find-by-genre", rel = "find-by-genre")
    List<Book> findByGenreContainingIgnoreCase(@Param("genre") String genre);

    /* hard-delete nativo: ignora qualsiasi filtro @Where */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM books WHERE id = :id", nativeQuery = true)
    int hardDeleteByIdNative(@Param("id") UUID id);

    /* endpoint HAL già esistente – lasciato com’è */
    List<Book> findByIsbn(String isbn);

    /* 🔍 Ricerca con filtri combinabili opzionali. */
    @Query("""
                SELECT b FROM Book b
                WHERE (:title IS NULL  OR b.title  ILIKE CONCAT('%', CAST(:title  AS string), '%'))
                  AND (:author IS NULL OR b.author ILIKE CONCAT('%', CAST(:author AS string), '%'))
                  AND (:genre IS NULL  OR b.genre  ILIKE CONCAT('%', CAST(:genre  AS string), '%'))
                  AND (:isbn IS NULL   OR b.isbn  = :isbn)
                  AND (:publicationYear IS NULL OR b.publicationYear = :publicationYear)
            """)
    List<Book> searchByOptionalFilters(
            @Param("title") String title,
            @Param("author") String author,
            @Param("genre") String genre,
            @Param("isbn") String isbn,
            @Param("publicationYear") Integer publicationYear);
}
