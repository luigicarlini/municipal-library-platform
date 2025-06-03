package it.comune.library.reservation.controller;

import it.comune.library.reservation.domain.Book;
import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.domain.HoldStatus;
import it.comune.library.reservation.dto.BookDto;
import it.comune.library.reservation.dto.HoldDetailsDto;
import it.comune.library.reservation.dto.HoldDto;
import it.comune.library.reservation.mapper.BookMapper;
import it.comune.library.reservation.mapper.HoldMapper;
import it.comune.library.reservation.repository.BookRepository;
import it.comune.library.reservation.repository.HoldRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/holds")
public class HoldController {

    private final HoldRepository holdRepository;
    private final HoldMapper holdMapper;
    private final BookMapper bookMapper;
    private final BookRepository bookRepository; // ✅ Iniettato per validazione bibId

    public HoldController(
            HoldRepository holdRepository,
            HoldMapper holdMapper,
            BookMapper bookMapper,
            BookRepository bookRepository // ✅ Aggiunto
    ) {
        this.holdRepository = holdRepository;
        this.holdMapper = holdMapper;
        this.bookMapper = bookMapper;
        this.bookRepository = bookRepository;
    }

    /**
     * ➕ Crea una nuova prenotazione Hold con validazione bibId e prevenzione duplicati.
     *
     * @param dto Dati della prenotazione in formato HoldDto.
     * @return HoldDto persistito oppure errore 400/409.
     */
    @PostMapping
    public ResponseEntity<?> createHold(@RequestBody HoldDto dto) {
        // ✅ Verifica se il libro esiste
        if (!bookRepository.existsById(dto.getBibId())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Book not found for bibId: " + dto.getBibId()
            ));
        }

        // ❌ Prevenzione duplicati: stesso patronId + bibId
        Optional<Hold> existing = holdRepository.findByPatronIdAndBibId(dto.getPatronId(), dto.getBibId());
        if (existing.isPresent()) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", "Duplicate hold: patron has already requested this book"
            ));
        }

        Hold hold = holdMapper.toEntity(dto);
        hold.setId(UUID.randomUUID());
        Hold saved = holdRepository.save(hold);
        return ResponseEntity.ok(holdMapper.toDto(saved));
    }

    /**
     * 🔍 Recupera una prenotazione tramite ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HoldDto> getHoldById(@PathVariable UUID id) {
        return holdRepository.findById(id)
                .map(hold -> ResponseEntity.ok(holdMapper.toDto(hold)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ✏️ Aggiorna una prenotazione esistente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<HoldDto> updateHold(@PathVariable UUID id, @RequestBody HoldDto dto) {
        Optional<Hold> optionalHold = holdRepository.findById(id);

        if (optionalHold.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Hold existing = optionalHold.get();

        // ✅ Solo i campi modificabili
        existing.setPickupBranch(dto.getPickupBranch());
        existing.setStatus(dto.getStatus());
        existing.setPosition(dto.getPosition());

        Hold updated = holdRepository.save(existing);
        return ResponseEntity.ok(holdMapper.toDto(updated));
    }

    /**
     * ❌ Soft-delete: imposta lo stato su CANCELLED.
     */
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelHold(@PathVariable UUID id) {
        Optional<Hold> optionalHold = holdRepository.findById(id);
        if (optionalHold.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Hold hold = optionalHold.get();
        hold.setStatus(HoldStatus.CANCELLED);
        holdRepository.save(hold);

        return ResponseEntity.noContent().build();
    }

    /**
     * ❌ Hard-delete: elimina fisicamente la prenotazione.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHold(@PathVariable UUID id) {
        if (!holdRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        holdRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 📚 Ottiene il libro associato a una prenotazione.
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
     * 🔍 Ricerca avanzata per campi combinati.
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

    /**
     * 📦 Dettagli completi prenotazione (Hold + Book).
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<HoldDetailsDto> getHoldDetails(@PathVariable UUID id) {
        Optional<Hold> optionalHold = holdRepository.findByIdWithBook(id);

        if (optionalHold.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Hold hold = optionalHold.get();
        HoldDto holdDto = holdMapper.toDto(hold);
        BookDto bookDto = bookMapper.toDto(hold.getBook());

        HoldDetailsDto details = new HoldDetailsDto();
        details.setHold(holdDto);
        details.setBook(bookDto);

        return ResponseEntity.ok(details);
    }
}
