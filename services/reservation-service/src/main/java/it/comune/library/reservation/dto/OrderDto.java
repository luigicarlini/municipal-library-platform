package it.comune.library.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 🛒 DTO che rappresenta un ordine di acquisto nel Book-shop.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dati di un ordine di acquisto libri")
public class OrderDto {

    @Schema(
        description = "Identificativo univoco dell’ordine",
        example = "42"
    )
    private Long id;

    @Schema(
        description = "ID del libro ordinato",
        example = "78df7ab2-8a8d-47e6-bce7-3da0ff61d6b9",
        format = "uuid"
    )
    private UUID bookId;

    @Schema(
        description = "ID del patron che ha effettuato l’ordine",
        example = "1001"
    )
    private Long patronId;

    @Schema(
        description = "Numero di copie acquistate",
        example = "2",
        minimum = "1"
    )
    private Integer quantity;

    @Schema(
        description = "Prezzo totale (snapshot al momento dell’ordine) in EUR",
        example = "29.98",
        format = "decimal"
    )
    private BigDecimal totalPrice;

    @Schema(
        description = "Stato corrente dell’ordine",
        example = "PAID",
        allowableValues = { "CREATED", "PENDING", "PAID", "FAILED", "CANCELLED" }
    )
    private String status;

    @Schema(
        description = "Timestamp di creazione dell’ordine (UTC)",
        example = "2025-06-05T13:45:27Z",
        format = "date-time"
    )
    private OffsetDateTime createdAt;
}
