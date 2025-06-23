package it.comune.library.reservation.repository;

import it.comune.library.reservation.domain.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository per l’entità {@link Book}.
 * <p>
 * • alias HAL legacy;<br>
 * • query avanzata con filtri combinabili, paginazione e ordinamento;<br>
 * • lock pessimista per update stock;<br>
 * • hard-delete che bypassa il soft-delete.
 */
@RepositoryRestResource(path = "books", collectionResourceRel = "books")
public interface BookRepository extends JpaRepository<Book, UUID> {

  /* ─────────── Alias legacy (/books/search/find-by-*) ─────────── */

  @RestResource(path = "find-by-title", rel = "find-by-title")
  List<Book> findByTitleContainingIgnoreCase(@Param("title") String title);

  @RestResource(path = "find-by-author", rel = "find-by-author")
  List<Book> findByAuthorContainingIgnoreCase(@Param("author") String author);

  @RestResource(path = "find-by-genre", rel = "find-by-genre")
  List<Book> findByGenreContainingIgnoreCase(@Param("genre") String genre);

  /* ─────────── Hard-delete (ignora @SQLDelete/@Where) ─────────── */

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = "DELETE FROM books WHERE id = :id", nativeQuery = true)
  int hardDeleteByIdNative(@Param("id") UUID id);

  /* Legge il flag deleted bypassando @Where */
  @Query(value = "SELECT deleted FROM books WHERE id = :id", nativeQuery = true)
  Boolean findDeletedFlagById(@Param("id") UUID id);

  /* Unicità ISBN su record attivi (deleted = false) */
  boolean existsByIsbnAndDeletedFalse(String isbn);

  /* Lock pessimista per aggiornare stock / version */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT b FROM Book b WHERE b.id = :id")
  Optional<Book> findByIdForUpdate(@Param("id") UUID id);

  // ↓ aggiungere alla interface BookRepository
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      UPDATE Book b
         SET b.stockQuantity = b.stockQuantity - :q,
             b.version       = b.version + 1
       WHERE b.id = :id
         AND b.stockQuantity >= :q
      """)
  int decrementStockIfEnough(@Param("id") UUID id,
      @Param("q") int q);

  /* ─────────── Ricerca con filtri + paginazione + sort ─────────── */

  @Query("""
      SELECT b FROM Book b
      WHERE b.deleted = false
        AND (:title IS NULL
             OR b.title  ILIKE CONCAT('%', CAST(:title  AS string), '%'))
        AND (:author IS NULL
             OR b.author ILIKE CONCAT('%', CAST(:author AS string), '%'))
        AND (:genre IS NULL
             OR b.genre  ILIKE CONCAT('%', CAST(:genre  AS string), '%'))
        AND (:isbn IS NULL  OR b.isbn = :isbn)
        AND (:publicationYear IS NULL OR b.publicationYear = :publicationYear)
      """)
  Page<Book> searchByOptionalFilters(@Param("title") String title,
      @Param("author") String author,
      @Param("genre") String genre,
      @Param("isbn") String isbn,
      @Param("publicationYear") Integer publicationYear,
      Pageable pageable);

  /* Overload “senza paginazione” (usato da vecchio controller) */
  default List<Book> searchByOptionalFilters(String title,
      String author,
      String genre,
      String isbn,
      Integer publicationYear) {
    return searchByOptionalFilters(title, author, genre, isbn,
        publicationYear, Pageable.unpaged())
        .getContent();
  }

  /* HAL generato da Spring-Data-REST */
  List<Book> findByIsbn(String isbn);

  /* Unicità ISBN globale (qualsiasi stato) */
  boolean existsByIsbn(String isbn);
}
