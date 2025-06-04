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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/holds")
@Tag(name = "HoldController", description = "Gestione delle prenotazioni (Hold)")
public class HoldController {

    private final HoldRepository holdRepository;
    private final HoldMapper holdMapper;
    private final BookMapper bookMapper;
    private final BookRepository bookRepository;

    public HoldController(
            HoldRepository holdRepository,
            HoldMapper holdMapper,
            BookMapper bookMapper,
            BookRepository bookRepository
    ) {
        this.holdRepository = holdRepository;
        this.holdMapper = holdMapper;
        this.bookMapper = bookMapper;
        this.bookRepository = bookRepository;
    }

    @Operation(summary = "Crea una nuova prenotazione", description = "Crea una Hold, valida l'esistenza del libro e previene duplicati.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prenotazione creata con successo"),
        @ApiResponse(responseCode = "400", description = "Il libro non esiste"),
        @ApiResponse(responseCode = "409", description = "Prenotazione duplicata")
    })
    @PostMapping
    public ResponseEntity<?> createHold(@RequestBody HoldDto dto) {
        if (!bookRepository.existsById(dto.getBibId())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Book not found for bibId: " + dto.getBibId()
            ));
        }

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

    @Operation(summary = "Recupera una prenotazione tramite ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prenotazione trovata"),
        @ApiResponse(responseCode = "404", description = "Prenotazione non trovata")
    })
    @GetMapping("/{id}")
    public ResponseEntity<HoldDto> getHoldById(@PathVariable UUID id) {
        return holdRepository.findById(id)
                .map(hold -> ResponseEntity.ok(holdMapper.toDto(hold)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Aggiorna una prenotazione esistente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prenotazione aggiornata"),
        @ApiResponse(responseCode = "404", description = "Prenotazione non trovata")
    })
    @PutMapping("/{id}")
    public ResponseEntity<HoldDto> updateHold(@PathVariable UUID id, @RequestBody HoldDto dto) {
        Optional<Hold> optionalHold = holdRepository.findById(id);
        if (optionalHold.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Hold existing = optionalHold.get();
        existing.setPickupBranch(dto.getPickupBranch());
        existing.setStatus(dto.getStatus());
        existing.setPosition(dto.getPosition());

        Hold updated = holdRepository.save(existing);
        return ResponseEntity.ok(holdMapper.toDto(updated));
    }

    @Operation(summary = "Cancella logicamente una prenotazione (soft-delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Prenotazione annullata"),
        @ApiResponse(responseCode = "404", description = "Prenotazione non trovata")
    })
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

    @Operation(summary = "Elimina definitivamente una prenotazione (hard-delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Prenotazione eliminata"),
        @ApiResponse(responseCode = "404", description = "Prenotazione non trovata")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHold(@PathVariable UUID id) {
        if (!holdRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        holdRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Recupera il libro associato a una prenotazione")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Libro trovato"),
        @ApiResponse(responseCode = "404", description = "Prenotazione o libro non trovato")
    })
    @GetMapping("/{id}/book")
    public ResponseEntity<BookDto> getBookForHold(@PathVariable UUID id) {
        return holdRepository.findById(id)
                .map(Hold::getBook)
                .map(bookMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Ricerca avanzata delle prenotazioni", description = "Permette di filtrare per titolo, autore, branch, status, genere, anno, posizione")
    @ApiResponse(responseCode = "200", description = "Risultati restituiti con successo")
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

    @Operation(summary = "Restituisce i dettagli completi della prenotazione (Hold + Book)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dettagli restituiti con successo"),
        @ApiResponse(responseCode = "404", description = "Prenotazione non trovata")
    })
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
