package it.comune.library.reservation.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import it.comune.library.reservation.validation.ISBN;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 📖 Entità JPA rappresentante un libro (biblioteca + Book-shop).
 */
@Entity
@Table(
    name = "books",
    /* unico vincolo che ci serve: isbn + deleted */
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"isbn", "deleted"})
    }
)
@SQLDelete(sql = "UPDATE books SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")          // esclude i soft-deleted in modo trasparente
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Libro")
public class Book {

    /* ─────────── PK ─────────── */
    @Id @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    /* ─────────── Metadati ─────────── */
    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(length = 100)
    private String genre;

    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;

    /* ─────────── Estensione Shop ─────────── */
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Min(0)
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    /* ❶  ——— rimosso unique=true ——— */
    @ISBN
    @Column(nullable = false, length = 17)
    private String isbn;

    /* ─────────── Soft-delete flag ─────────── */
    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deleted = false;

    /* ─────────── Optimistic locking ─────────── */
    @Version
    @Column(nullable = false)
    private Integer version;
}
