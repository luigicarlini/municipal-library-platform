package it.comune.library.reservation.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

import it.comune.library.reservation.domain.Book; // ✅ IMPORT ESSENZIALE

@Entity
@Table(name = "holds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hold {

    @Id
    private UUID id;

    @Column(name = "hold_id", nullable = false, unique = true)
    private UUID holdId;

    @Column(name = "patron_id", nullable = false)
    private UUID patronId;

    @Column(name = "bib_id", nullable = false)
    private UUID bibId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bib_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Book book;

    @Column(name = "pickup_branch", nullable = false)
    private String pickupBranch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HoldStatus status;

    private Integer position;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        if (this.holdId == null) {
            this.holdId = this.id;
        }
    }
}
