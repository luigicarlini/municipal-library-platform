package it.comune.library.reservation.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import it.comune.library.reservation.validation.ISBN;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.*;

import org.hibernate.annotations.SQLDelete;          // ← import Hibernate
import org.hibernate.annotations.Where;             // ← import Hibernate

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 📖 Entità JPA rappresentante un libro (biblioteca + Book-shop).
 */
@Entity
@Table(name = "books")
@SQLDelete(sql = "UPDATE books SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")                  // esclude i soft-deleted
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

    @ISBN
    @Column(nullable = false, unique = true, length = 17)
    private String isbn;

    /* ─────────── Soft-delete flag ─────────── */
    @Builder.Default                               // valore default anche con Lombok-builder
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deleted = false;

    /* ─────────── Optimistic locking ─────────── */
    @Version
    private Integer version;
}
