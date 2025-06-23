// src/main/java/it/comune/library/reservation/exception/InsufficientStockException.java
package it.comune.library.reservation.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String msg) { super(msg); }
}

