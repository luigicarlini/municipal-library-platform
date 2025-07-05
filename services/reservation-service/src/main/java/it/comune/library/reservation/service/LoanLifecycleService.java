package it.comune.library.reservation.service;

import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.domain.HoldStatus;
import it.comune.library.reservation.repository.HoldRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanLifecycleService {

    private final HoldRepository repo;

    /* ------------------------------------------------------------------
     * 1) Batch giornaliero: marca come OVERDUE tutti i prestiti scaduti
     * ------------------------------------------------------------------ */
    @Transactional
    @Scheduled(cron = "0 15 3 * * *")       // ogni giorno alle 03:15
    public void sweepOverdueJob() {
        int rows = sweepOverdue(LocalDate.now());
        if (rows > 0) {
            log.info("Sweep overdue: {} hold passati a OVERDUE", rows);
        }
    }

    /** Metodo testabile (inietti un Clock finto) */
    @Transactional
    public int sweepOverdue(LocalDate today) {
        return repo.updateStatusOverdue(today);
    }

    /* ------------------------------------------------------------------
     * 2) Restituzione manuale di un libro
     * ------------------------------------------------------------------ */
    @Transactional
    public void markReturned(UUID holdId) {

        Hold h = repo.findById(holdId)
                     .orElseThrow(() ->
                         new EntityNotFoundException("Hold " + holdId + " non trovato"));

        if (h.getStatus() == HoldStatus.CANCELLED) {
            throw new IllegalStateException("Hold cancellato; impossibile registrare il reso");
        }
        h.setStatus(HoldStatus.RETURNED);
    }
}