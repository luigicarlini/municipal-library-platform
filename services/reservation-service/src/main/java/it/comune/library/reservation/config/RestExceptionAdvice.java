package it.comune.library.reservation.config;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.apache.commons.lang3.exception.ExceptionUtils;
import jakarta.validation.ConstraintViolationException;

/**
 * Cattura le eccezioni comuni e restituisce sempre JSON:
 * { "status": <codice>, "error": "<messaggio>" }
 */
@RestControllerAdvice
public class RestExceptionAdvice {

    /* 404 – entity non trovata */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> notFound(EntityNotFoundException ex) {
        return body(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /* 400 – validazione DTO fallita */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> badRequest(MethodArgumentNotValidException ex) {
        return body("Validation error", HttpStatus.BAD_REQUEST);
    }

    /* 409 – violazione unique‐constraint o optimistic‐lock */
    @ExceptionHandler({
            DataIntegrityViolationException.class,
            org.springframework.orm.ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<ApiError> conflict(Exception ex) {
        return body(ex.getMessage(), HttpStatus.CONFLICT);
    }

    /* fallback 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unknown(Exception ex) {
        return body("Internal error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static ResponseEntity<ApiError> body(String msg, HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body(new ApiError(status.value(), msg));
    }

    /* 409 – Optimistic lock gestito direttamente dall’eccezione */
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(OptimisticLockException ex) {

        /*
         * manteniamo il messaggio originale (se presente) ma
         * anteponiamo la keyword che il test si aspetta
         */
        String msg = "Optimistic lock – " +
                (ex.getMessage() != null
                        ? ex.getMessage()
                        : "versione non aggiornata: l’entità è stata modificata da un altro utente.");

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(HttpStatus.CONFLICT.value(), msg));
    }

    /* Transaction rollback generico */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiError> handleTx(TransactionSystemException ex) {

        Throwable root = ExceptionUtils.getRootCause(ex);

        /* 400 – Bean-Validation */
        if (root instanceof jakarta.validation.ConstraintViolationException v) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiError(HttpStatus.BAD_REQUEST.value(),
                            "Validation error: " + v.getMessage()));
        }

        /* 409 – Optimistic lock propagato via TransactionSystemException */
        if (root instanceof jakarta.persistence.OptimisticLockException) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ApiError(HttpStatus.CONFLICT.value(),
                            "Optimistic lock – il libro è stato modificato da un altro utente. "
                                    + "Ricarica e riprova."));
        }

        /* 500 – altro errore di transazione */
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Errore di transazione imprevisto."));
    }

    /* Rollback dal transaction manager (es. propagation=NESTED) */
    @ExceptionHandler(UnexpectedRollbackException.class)
    public ResponseEntity<ApiError> unexpectedTx(UnexpectedRollbackException ex) {

        Throwable root = ExceptionUtils.getRootCause(ex);

        if (root instanceof ConstraintViolationException v) {
            return body("Validation error: " + v.getMessage(), HttpStatus.BAD_REQUEST);
        }

        /* Se arrivasse un Optimistic lock da qui lo etichettiamo comunque */
        if (root instanceof OptimisticLockException) {
            return body("Optimistic lock – " + root.getMessage(), HttpStatus.CONFLICT);
        }

        return body("Internal error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public record ApiError(int status, String message) {
    }
}
