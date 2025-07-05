package it.comune.library.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry-point del micro-servizio *reservation-service*.
 * L’annotazione {@code @EnableScheduling} abilita l’esecuzione di tutti
 * i metodi annotati con {@code @Scheduled}, come lo sweep giornaliero
 * degli hold scaduti definito in {@code LoanLifecycleService}.
 */
@SpringBootApplication
@EnableScheduling          // ← abilita i job @Scheduled
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
}