package it.comune.library.reservation.controller;

import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.domain.HoldStatus;
import it.comune.library.reservation.dto.BookDto;
import it.comune.library.reservation.dto.HoldDto;
import it.comune.library.reservation.mapper.BookMapper;
import it.comune.library.reservation.mapper.HoldMapper;
import it.comune.library.reservation.repository.HoldRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/holds")
public class HoldController {

    private final HoldRepository holdRepository;
    private final HoldMapper holdMapper;
    private final BookMapper bookMapper;

    public HoldController(HoldRepository holdRepository, HoldMapper holdMapper, BookMapper bookMapper) {
        this.holdRepository = holdRepository;
        this.holdMapper = holdMapper;
        this.bookMapper = bookMapper;
    }

    /**
     * ➕ Crea una nuova prenotazione Hold.
     *
     * @param dto Dati della prenotazione in formato HoldDto.
     * @return HoldDto persistito.
     */
    @PostMapping
    public ResponseEntity<HoldDto> createHold(@RequestBody HoldDto dto) {
        Hold hold = holdMapper.toEntity(dto);
        hold.setId(UUID.randomUUID());
        Hold saved = holdRepository.save(hold);
        return ResponseEntity.ok(holdMapper.toDto(saved));
    }

    /**
     * 🔍 Recupera una prenotazione tramite ID.
     *
     * @param id Identificativo della prenotazione.
     * @return HoldDto trovato oppure 404.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HoldDto> getHoldById(@PathVariable UUID id) {
        return holdRepository.findById(id)
                .map(hold -> ResponseEntity.ok(holdMapper.toDto(hold)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 📚 Ottiene il libro associato a una prenotazione.
     *
     * @param id ID della prenotazione.
     * @return BookDto oppure 404 se non associato.
     */
    @GetMapping("/{id}/book")
    public ResponseEntity<BookDto> getBookForHold(@PathVariable UUID id) {
        return holdRepository.findById(id)
                .map(Hold::getBook)
                .map(bookMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 🔍 Ricerca avanzata con filtri combinabili.
     *
     * @param title Titolo del libro.
     * @param author Autore del libro.
     * @param pickupBranch Sede di ritiro.
     * @param status Stato della prenotazione.
     * @param genre Genere del libro.
     * @param publicationYear Anno di pubblicazione.
     * @param position Posizione nella coda.
     * @return Lista di prenotazioni che rispettano i filtri.
     */
    @GetMapping
    public ResponseEntity<List<HoldDto>> searchHolds(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String pickupBranch,
            @RequestParam(required = false) HoldStatus status,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer publicationYear,
            @RequestParam(required = false) Integer position
    ) {
        List<Hold> results = holdRepository.searchByOptionalFilters(
                title, author, pickupBranch, status, genre, publicationYear, position
        );

        return ResponseEntity.ok(results.stream()
                .map(holdMapper::toDto)
                .collect(Collectors.toList()));
    }
}
