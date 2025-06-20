package it.comune.library.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(description = "Dati del libro")
public class BookDto {

    @Schema(description = "Identificativo unico del libro", example = "78df7ab2-8a8d-47e6-bce7-3da0ff61d6b9")
    private UUID id;

    @Schema(description = "Titolo del libro", example = "Orgoglio e pregiudizio")
    private String title;

    @Schema(description = "Autore del libro", example = "Jane Austen")
    private String author;

    @Schema(description = "Genere letterario del libro", example = "Romanzo")
    private String genre;

    @Schema(description = "Anno di pubblicazione", example = "1813")
    private Integer publicationYear;

    @Schema(description = "Codice ISBN", example = "9788807900386")
    private String isbn;

    @Schema(description = "Prezzo di vendita in EUR", example = "14.99")
    private BigDecimal price; // ✅ nuovo

    @Schema(description = "Copie disponibili alla vendita", example = "5")
    private Integer stockQuantity; // ✅ nuovo

    @Schema(description = "Versione ottimistic locking")
    private Integer version;

    @Schema(description = "Flag di soft-delete", hidden = true)
    private Boolean deleted = false;
}
