package it.comune.library.reservation.domain;

/**
 * Stato di una prenotazione (Hold) lungo l’intero ciclo di vita.
 */
public enum HoldStatus {
    PLACED,     // richiesta inserita
    READY,      // pronto al ritiro
    OVERDUE,    // non riconsegnato entro la due_date     ← NEW
    RETURNED,   // restituito                              ← NEW
    CANCELLED   // annullato dall’utente o per soft-delete
}
