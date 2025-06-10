package it.comune.library.reservation.repository;

import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.domain.HoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

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

    /**
     * Carica una prenotazione con il relativo libro associato (JOIN FETCH).
     */
    @Query("SELECT h FROM Hold h JOIN FETCH h.book WHERE h.id = :id")
    Optional<Hold> findByIdWithBook(@Param("id") UUID id);

    /**
     * Trova una prenotazione per utente e libro (usato per evitare duplicati).
     */
    Optional<Hold> findByPatronIdAndBibId(UUID patronId, UUID bibId);

    /**
     * Metodo semantico per aggiornare una prenotazione.
     * Utile per distinguere logicamente il salvataggio da una creazione.
     *
     * @param hold prenotazione da aggiornare
     * @return prenotazione aggiornata
     */
    default Hold updateHold(Hold hold) {
        return save(hold);
    }
}
