package it.comune.library.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.comune.library.reservation.domain.HoldStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Schema(description = "Dati della prenotazione Hold")
public class HoldDto {

    @Schema(description = "Identificativo univoco della prenotazione", example = "5be310da-e78e-4d29-a555-dc16c9820a88")
    private UUID id;

    @Schema(description = "ID dell’utente che ha effettuato la prenotazione", example = "19b81f1c-70fe-45df-be26-876af053f88b")
    private UUID patronId;

    @Schema(description = "ID del libro prenotato", example = "3fc24a80-c82c-4e0a-97eb-7830fb1fc746")
    private UUID bibId;

    @Schema(description = "Sede di ritiro della prenotazione", example = "Centrale")
    private String pickupBranch;

    @Schema(description = "Stato attuale della prenotazione", example = "PLACED")
    private HoldStatus status;

    @Schema(description = "Posizione nella coda di prenotazione", example = "1")
    private Integer position;

    @Schema(description = "Data e ora di creazione della prenotazione", example = "2025-05-22T11:44:50.544091Z")
    private Instant createdAt;
}