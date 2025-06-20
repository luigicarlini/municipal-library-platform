package it.comune.library.reservation.domain;

public enum HoldStatus {
    PLACED,     // in coda
    READY,
    COLLECTED,
    CANCELLED,  // annullato per soft-delete libro o azione utente
    EXPIRED
}

