package it.comune.library.reservation.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "holds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "📦 Entità JPA che rappresenta una prenotazione (Hold) di un libro")
public class Hold {

    @Id
    @GeneratedValue
    @Schema(description = "ID interno generato della prenotazione", example = "5be310da-e78e-4d29-a555-dc16c9820a88")
    private UUID id;

    @Column(name = "hold_id", nullable = false, unique = true)
    @Schema(description = "ID pubblico visibile della prenotazione", example = "5be310da-e78e-4d29-a555-dc16c9820a88")
    private UUID holdId;

    @Column(name = "patron_id", nullable = false)
    @Schema(description = "ID dell'utente che ha effettuato la prenotazione", example = "19b81f1c-70fe-45df-be26-876af053f88b")
    private UUID patronId;

    @Column(name = "bib_id", nullable = false)
    @Schema(description = "ID del libro prenotato", example = "3fc24a80-c82c-4e0a-97eb-7830fb1fc746")
    private UUID bibId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bib_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Schema(description = "Riferimento al libro associato alla prenotazione")
    private Book book;

    @Column(name = "pickup_branch", nullable = false)
    @Schema(description = "Filiale di ritiro scelta dall'utente", example = "Centrale")
    private String pickupBranch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Stato attuale della prenotazione", example = "PLACED")
    private HoldStatus status;

    @Schema(description = "Posizione nella coda di prenotazione", example = "1")
    private Integer position;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Timestamp di creazione della prenotazione", example = "2025-05-22T11:44:50.544091Z")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        if (this.holdId == null) {
            this.holdId = this.id;
        }
    }
}