package it.comune.library.reservation.domain;

/** Stati del ciclo di vita di un Order. */
public enum OrderStatus {
    CREATED,      // appena creato, stock “prenotato”
    PENDING,      // stiamo aspettando la conferma del gateway
    PAID,         // pagamento riuscito
    FAILED,       // pagamento rifiutato/timeout
    CANCELLED     // annullato prima del pagamento
}

