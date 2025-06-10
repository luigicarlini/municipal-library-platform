package it.comune.library.reservation.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import it.comune.library.reservation.validation.ISBN;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 📖 Entità JPA rappresentante un libro nella biblioteca, estesa per la funzionalità Bookshop.
 */
@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "📖 Entità JPA rappresentante un libro nella biblioteca")
public class Book {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    @Schema(description = "ID univoco del libro", example = "78df7ab2-8a8d-47e6-bce7-3da0ff61d6b9")
    private UUID id;

    @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(255)")
    @Schema(description = "Titolo del libro", example = "Orgoglio e pregiudizio")
    private String title;

    @Column(name = "author", nullable = false, columnDefinition = "VARCHAR(255)")
    @Schema(description = "Autore del libro", example = "Jane Austen")
    private String author;

    @Column(name = "genre", columnDefinition = "VARCHAR(100)")
    @Schema(description = "Genere letterario del libro", example = "Romanzo")
    private String genre;

    @Column(name = "publication_year", nullable = false)
    @Schema(description = "Anno di pubblicazione", example = "1813")
    private Integer publicationYear;

    /* ------------ ESTENSIONE BOOKSHOP ------------ */

    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    @Schema(description = "Prezzo di vendita in EUR", example = "14.99")
    private BigDecimal price;

    @Min(0)
    @Column(name = "stock_quantity", nullable = false)
    @Schema(description = "Copie disponibili alla vendita", example = "5")
    private Integer stockQuantity;

    @ISBN
    @Column(nullable = false, unique = true, length = 17)
    @Schema(description = "Codice ISBN valido (ISBN-10 o ISBN-13)", example = "9788807900386")
    private String isbn;

    /* ------------ OTTIMISTIC LOCK ------------ */

    @Version
    @Schema(description = "Versione per il locking ottimistico")
    private Integer version;
}
