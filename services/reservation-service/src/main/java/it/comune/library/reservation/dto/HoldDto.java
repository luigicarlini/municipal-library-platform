package it.comune.library.reservation.dto;

import it.comune.library.reservation.domain.HoldStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class HoldDto {
    private UUID id;
    private UUID patronId;
    private UUID bibId;
    private String pickupBranch;
    private HoldStatus status;
    private Integer position;
    private Instant createdAt;
}

