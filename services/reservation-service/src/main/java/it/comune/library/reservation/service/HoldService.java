package it.comune.library.reservation.service;

import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.domain.HoldStatus;
import it.comune.library.reservation.dto.HoldDto;
import it.comune.library.reservation.mapper.HoldMapper;
import it.comune.library.reservation.repository.BookRepository;
import it.comune.library.reservation.repository.HoldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HoldService {

    private final HoldRepository holdRepo;
    private final BookRepository bookRepo;
    private final HoldMapper holdMapper;

    /**
     * Crea una nuova HOLD in stato PLACED.
     * Se il libro non esiste o è soft-deleted → IllegalStateException.
     */
    @Transactional
    public HoldDto create(HoldDto dto) {
        UUID bibId = dto.getBibId();

        Book book = bookRepo.findById(bibId)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new IllegalStateException("Book deleted or not found"));

        long count = holdRepo.countByBibIdAndStatus(bibId, HoldStatus.PLACED);
        int nextPos = (int) (count + 1);

        Hold hold = Hold.builder()
                .bibId(bibId)
                .patronId(dto.getPatronId())
                .pickupBranch(dto.getPickupBranch())
                .status(HoldStatus.PLACED)
                .position(nextPos)
                .build();

        Hold saved = holdRepo.save(hold);
        return holdMapper.toDto(saved);
    }

    // … altri metodi di servizio …
}
