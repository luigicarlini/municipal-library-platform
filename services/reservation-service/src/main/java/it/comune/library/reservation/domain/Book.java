package it.comune.library.reservation.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    private UUID id;

@Column(name = "title", nullable = false, columnDefinition = "VARCHAR(255)")
private String title;


@Column(name = "author", nullable = false, columnDefinition = "VARCHAR(255)")
private String author;


@Column(name = "genre", nullable = true, columnDefinition = "VARCHAR(100)")
private String genre;


@Column(name = "publication_year", nullable = false)
private Integer publicationYear; // ✅ AGGIUNTO QUESTO CAMPO

@Column(name = "isbn", nullable = true, columnDefinition = "VARCHAR(20)")
private String isbn;

}

