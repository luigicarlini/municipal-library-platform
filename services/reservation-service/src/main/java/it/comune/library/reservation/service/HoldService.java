package it.comune.library.reservation.service;

import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.domain.HoldStatus;
import it.comune.library.reservation.dto.HoldDto;
import it.comune.library.reservation.mapper.HoldMapper;
import it.comune.library.reservation.repository.BookRepository;
import it.comune.library.reservation.repository.HoldRepository;
import it.comune.library.reservation.repository.HoldSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HoldService {

    private final HoldRepository holdRepo;
    private final BookRepository bookRepo;
    private final HoldMapper holdMapper;

    /* --------------------------------------------------------------------
     * 1. Creazione di una prenotazione (stato = PLACED)
     * ------------------------------------------------------------------ */
    @Transactional
    public HoldDto create(HoldDto dto) {

        UUID bibId = dto.getBibId();

        // libro esistente e non soft-deleted?
        Book book = bookRepo.findById(bibId)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() ->
                        new IllegalStateException("Book deleted or not found"));

        // posizione nella coda (“PLACED”)
        long count = holdRepo.countByBibIdAndStatus(bibId, HoldStatus.PLACED);
        int nextPos = (int) count + 1;

        Hold hold = Hold.builder()
                .bibId(bibId)
                .patronId(dto.getPatronId())
                .pickupBranch(dto.getPickupBranch())
                .status(HoldStatus.PLACED)
                .position(nextPos)
                .build();

        return holdMapper.toDto(holdRepo.save(hold));
    }

    /* --------------------------------------------------------------------
     * 2. Ricerca paginata con filtri facoltativi
     * ------------------------------------------------------------------ */
    @Transactional(readOnly = true)
    public Page<HoldDto> searchPaged(
            HoldStatus status,
            String pickupBranch,
            int page,
            int size,
            Sort sort) {

        Pageable pageable = PageRequest.of(page, size, sort);

        // costruzione Specification dinamica
        Specification<Hold> spec = Specification.where(HoldSpecs.bookNotDeleted());

        if (status != null) {
            spec = spec.and(HoldSpecs.byStatus(status));
        }
        if (pickupBranch != null && !pickupBranch.isBlank()) {
            spec = spec.and(HoldSpecs.byPickupBranch(pickupBranch.trim()));
        }

        Page<Hold> result = holdRepo.findAll(spec, pageable);

        return result.map(holdMapper::toDto);           // mapping -> DTO
    }

    /* --------------------------------------------------------------------
     * 3. Convenienza: restituisce solo il conteggio per *gli stessi filtri*
     *    (utile per intestazione X-Total-Count senza rileggere l’intera page)
     * ------------------------------------------------------------------ */
    @Transactional(readOnly = true)
    public long count(HoldStatus status, String pickupBranch) {

        Specification<Hold> spec = Specification.where(HoldSpecs.bookNotDeleted());

        if (status != null) {
            spec = spec.and(HoldSpecs.byStatus(status));
        }
        if (pickupBranch != null && !pickupBranch.isBlank()) {
            spec = spec.and(HoldSpecs.byPickupBranch(pickupBranch.trim()));
        }
        return holdRepo.count(spec);
    }

    /* --------------------------------------------------------------------
     * … eventuali altri metodi legacy già presenti (no-change) …
     * ------------------------------------------------------------------ */
}
