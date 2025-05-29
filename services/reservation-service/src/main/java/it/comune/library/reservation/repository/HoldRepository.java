package it.comune.library.reservation.repository;

import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.domain.HoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HoldRepository extends JpaRepository<Hold, UUID> {

    /**
     * Ricerca per titolo del libro.
     */
    @Query("SELECT h FROM Hold h JOIN h.book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Hold> findByBookTitleContainingIgnoreCase(@Param("title") String title);

    /**
     * Ricerca per autore del libro.
     */
    @Query("SELECT h FROM Hold h JOIN h.book b WHERE LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))")
    List<Hold> findByBookAuthorContainingIgnoreCase(@Param("author") String author);

    /**
     * Ricerca per sede di ritiro.
     */
    List<Hold> findByPickupBranchIgnoreCase(String pickupBranch);

    /**
     * Ricerca avanzata con filtri combinabili su libro e prenotazione.
     */
    @Query("""
        SELECT h FROM Hold h
        JOIN h.book b
        WHERE
            (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%')))
            AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', CAST(:author AS string), '%')))
            AND (:pickupBranch IS NULL OR LOWER(h.pickupBranch) = LOWER(CAST(:pickupBranch AS string)))
            AND (:status IS NULL OR h.status = :status)
            AND (:genre IS NULL OR LOWER(b.genre) = LOWER(CAST(:genre AS string)))
            AND (:publicationYear IS NULL OR b.publicationYear = :publicationYear)
            AND (:position IS NULL OR h.position = :position)
    """)
    List<Hold> searchByOptionalFilters(
            @Param("title") String title,
            @Param("author") String author,
            @Param("pickupBranch") String pickupBranch,
            @Param("status") HoldStatus status,
            @Param("genre") String genre,
            @Param("publicationYear") Integer publicationYear,
            @Param("position") Integer position
    );
}
