package it.comune.library.reservation.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class BookDto {
    private UUID id;
    private String title;
    private String author;
    private String genre;
    private Integer publicationYear;
    private String isbn;
}

