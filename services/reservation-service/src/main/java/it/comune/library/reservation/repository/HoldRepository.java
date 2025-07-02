package it.comune.library.reservation.repository;

import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.domain.HoldStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository JPA per {@link Hold}.
 */
@Repository
public interface HoldRepository
    extends JpaRepository<Hold, UUID>,
    JpaSpecificationExecutor<Hold> {

  /*
   * ───────────────────────────────
   * LEGACY / specific finders
   * ──────────────────────────────
   */
  @Query("""
      SELECT h FROM Hold h
      JOIN h.book b
      WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))
      """)
  List<Hold> findByBookTitleContainingIgnoreCase(@Param("title") String title);

  @Query("""
      SELECT h FROM Hold h
      JOIN h.book b
      WHERE LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))
      """)
  List<Hold> findByBookAuthorContainingIgnoreCase(@Param("author") String author);

  List<Hold> findByPickupBranchIgnoreCase(String pickupBranch);

  long countByBibIdAndStatus(UUID bibId, HoldStatus status);

  /*
   * ───────────────────────────────
   * Ricerca avanzata – LIST (no paging)
   * ──────────────────────────────
   */
  @Query("""
      SELECT h FROM Hold h
      JOIN h.book b
      WHERE (:title IS NULL           OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:author IS NULL          OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')))
        AND (:pickupBranch IS NULL    OR LOWER(h.pickupBranch) = LOWER(:pickupBranch))
        AND (:status IS NULL          OR h.status               = :status)
        AND (:genre IS NULL           OR LOWER(b.genre)         = LOWER(:genre))
        AND (:publicationYear IS NULL OR b.publicationYear      = :publicationYear)
        AND (:position IS NULL        OR h.position             = :position)
      """)
  List<Hold> searchByOptionalFilters(
      @Param("title") String title,
      @Param("author") String author,
      @Param("pickupBranch") String pickupBranch,
      @Param("status") HoldStatus status,
      @Param("genre") String genre,
      @Param("publicationYear") Integer publicationYear,
      @Param("position") Integer position);

  /*
   * ───────────────────────────────
   * Ricerca avanzata – PAGINATED
   * ──────────────────────────────
   */
  @Query("""
      SELECT h FROM Hold h
      JOIN h.book b
      WHERE (:title IS NULL           OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:author IS NULL          OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')))
        AND (:pickupBranch IS NULL    OR LOWER(h.pickupBranch) = LOWER(:pickupBranch))
        AND (:status IS NULL          OR h.status               = :status)
        AND (:genre IS NULL           OR LOWER(b.genre)         = LOWER(:genre))
        AND (:publicationYear IS NULL OR b.publicationYear      = :publicationYear)
      """)
  Page<Hold> searchPaged(
      @Param("title") String title,
      @Param("author") String author,
      @Param("pickupBranch") String pickupBranch,
      @Param("status") HoldStatus status,
      @Param("genre") String genre,
      @Param("publicationYear") Integer publicationYear,
      Pageable pageable);

  /*
   * ───────────────────────────────
   * Aggiornamenti bulk
   * ──────────────────────────────
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      UPDATE Hold h
         SET h.status = it.comune.library.reservation.domain.HoldStatus.CANCELLED
       WHERE h.book.id = :bookId
         AND h.status <> it.comune.library.reservation.domain.HoldStatus.CANCELLED
      """)
  int cancelActiveHoldsForBook(@Param("bookId") UUID bookId);

  long countByStatusAndBookId(HoldStatus status, UUID bookId);

  long countByBibId(UUID bibId);

  @Modifying
  @Query(value = "DELETE FROM books WHERE id = :id", nativeQuery = true)
  int hardDeleteByIdNative(@Param("id") UUID id);

  /*
   * ───────────────────────────────
   * Utility
   * ──────────────────────────────
   */
  @Query("SELECT h FROM Hold h JOIN FETCH h.book WHERE h.id = :id")
  Optional<Hold> findByIdWithBook(@Param("id") UUID id);

  Optional<Hold> findByPatronIdAndBibId(UUID patronId, UUID bibId);

  @Query("""
      SELECT COALESCE(MAX(h.position), 0)
      FROM Hold h
      WHERE h.bibId = :bibId
        AND h.status = 'PLACED'
      """)
  int findMaxPositionByBibId(@Param("bibId") UUID bibId);
}
