package it.comune.library.reservation.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

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

    @Column(name = "genre", nullable = true, columnDefinition = "VARCHAR(100)")
    @Schema(description = "Genere letterario del libro", example = "Romanzo")
    private String genre;

    @Column(name = "publication_year", nullable = false)
    @Schema(description = "Anno di pubblicazione", example = "1813")
    private Integer publicationYear;

    @Column(name = "isbn", nullable = true, columnDefinition = "VARCHAR(20)")
    @Schema(description = "Codice ISBN del libro", example = "9788807900386")
    private String isbn;
}