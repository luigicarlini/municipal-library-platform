package it.comune.library.reservation.dto;

import it.comune.library.reservation.domain.HoldStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO per la risorsa Hold.
 * Utilizzato per trasferire i dati da/verso il client senza esporre l'entità JPA direttamente.
 */
@Data
public class HoldDto {

    // Identificativo univoco del hold (PK)
    private UUID id;

    // Identificativo dell'utente che ha effettuato la prenotazione
    private UUID patronId;

    // ID del libro associato (corrisponde a Book.id)
    private UUID bibId;

    // Nome della filiale presso cui il libro sarà ritirato
    private String pickupBranch;

    // Stato corrente della prenotazione (READY, CANCELLED, IN_PROGRESS, ecc.)
    private HoldStatus status;

    // Posizione nella coda della prenotazione (opzionale)
    private Integer position;

    // Timestamp della creazione della prenotazione (generato automaticamente)
    private Instant createdAt;
}