package it.comune.library.reservation.dto;

import it.comune.library.reservation.domain.HoldStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "DTO per aggiornamento parziale della prenotazione Hold")
@Data
public class HoldUpdateDto {

    @Schema(description = "Nuovo stato della prenotazione", example = "CANCELLED")
    private HoldStatus status;

    @Schema(description = "Nuova filiale per il ritiro", example = "CENTRAL")
    private String pickupBranch;

    @Schema(description = "Nuova posizione in coda", example = "2")
    private Integer position;
}

