package it.comune.library.reservation.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * 📦 Entità JPA che rappresenta una prenotazione (“hold”).
 */
@Entity
@Table(name = "holds")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Prenotazione di un libro (hold)")
public class Hold {

    /** Chiave tecnica interna (PK) */
    @Id @GeneratedValue
    @Schema(example = "5be310da-e78e-4d29-a555-dc16c9820a88")
    private UUID id;

    /** Identificativo pubblico esposto alle API */
    @Column(name = "hold_id", nullable = false, unique = true)
    @Schema(example = "5be310da-e78e-4d29-a555-dc16c9820a88")
    private UUID holdId;

    /** Utente che ha effettuato la prenotazione */
    @Column(name = "patron_id", nullable = false)
    @Schema(example = "19b81f1c-70fe-45df-be26-876af053f88b")
    private UUID patronId;

    /** FK al libro prenotato (colonna bib_id) */
    @Column(name = "bib_id", nullable = false)
    @Schema(example = "3fc24a80-c82c-4e0a-97eb-7830fb1fc746")
    private UUID bibId;

    /** Associazione JPA 👉 serve per join/filter */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bib_id", referencedColumnName = "id",
                insertable = false, updatable = false)
    private Book book;

    /** Filiale di ritiro */
    @Column(name = "pickup_branch", nullable = false)
    @Schema(example = "CENTRAL")
    private String pickupBranch;

    /** Stato attuale */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(example = "PLACED")
    private HoldStatus status;

    /** Posizione in coda (facoltativa) */
    private Integer position;

    /** Timestamp creazione */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /* ---------------------------- lifecycle ---------------------------- */
    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        if (this.holdId == null) {          // rende id “pubblico” stabile
            this.holdId = UUID.randomUUID();
        }
    }
}
