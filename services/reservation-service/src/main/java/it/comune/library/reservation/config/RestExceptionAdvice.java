package it.comune.library.reservation.config;      // ✅ package giusto!

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Intercetta le eccezioni comuni dell’applicazione e
 * restituisce un JSON con { "error": "...", "status": n }.
 */
@RestControllerAdvice
public class RestExceptionAdvice {

    /* 404 – entity non trovata */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> notFound(EntityNotFoundException ex) {
        return body(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /* 400 – validazione DTO fallita */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> badRequest(MethodArgumentNotValidException ex) {
        return body("Validation error", HttpStatus.BAD_REQUEST);
    }

    /* 409 – violazione constraint DB / optimistic-lock */
    @ExceptionHandler({
            DataIntegrityViolationException.class,
            org.springframework.orm.ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<?> conflict(Exception ex) {
        return body(ex.getMessage(), HttpStatus.CONFLICT);
    }

    /* fallback 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unknown(Exception ex) {
        return body("Internal error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /* helper */
    private static ResponseEntity<?> body(String msg, HttpStatus status) {
        return ResponseEntity.status(status)
                             .body(new ApiError(status.value(), msg));
    }

    /* POJO minimale per la risposta JSON */
    record ApiError(int status, String error) {}
}
