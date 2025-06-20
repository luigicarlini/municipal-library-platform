package it.comune.library.reservation.controller;

import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.domain.HoldStatus;
import it.comune.library.reservation.dto.*;
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
    private final HoldMapper     holdMapper;
    private final BookMapper     bookMapper;
    private final BookRepository bookRepository;

    public HoldController(HoldRepository holdRepository,
                          HoldMapper holdMapper,
                          BookMapper bookMapper,
                          BookRepository bookRepository) {
        this.holdRepository  = holdRepository;
        this.holdMapper      = holdMapper;
        this.bookMapper      = bookMapper;
        this.bookRepository  = bookRepository;
    }

    @Operation(summary = "Crea una nuova prenotazione",
               description = "Crea una Hold, valida l'esistenza del libro e previene duplicati.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prenotazione creata con successo"),
        @ApiResponse(responseCode = "409", description = "Libro non disponibile o prenotazione duplicata")
    })
    @PostMapping
    public ResponseEntity<?> createHold(@RequestBody HoldDto dto) {
        // → se il libro non è presente (o è soft-deleted) → 409
        if (!bookRepository.existsById(dto.getBibId())) {
            return ResponseEntity.status(409).body(Map.of(
                "error", "Book not available for bibId: " + dto.getBibId()));
        }

        // → duplicato di hold
        Optional<Hold> existing =
            holdRepository.findByPatronIdAndBibId(dto.getPatronId(), dto.getBibId());
        if (existing.isPresent()) {
            return ResponseEntity.status(409).body(Map.of(
                "error", "Duplicate hold: patron has already requested this book"));
        }

        // mappatura e inizializzazioni
        Hold hold = holdMapper.toEntity(dto);
        hold.setId(UUID.randomUUID());
        hold.setStatus(HoldStatus.PLACED);

        // Calcolo posizione in coda
        int nextPos = holdRepository.findMaxPositionByBibId(dto.getBibId()) + 1;
        hold.setPosition(nextPos);

        Hold saved = holdRepository.save(hold);
        return ResponseEntity.ok(holdMapper.toDto(saved));
    }

    /*──────────────────────────────────────────────
     * 2.  READ (by ID)
     *─────────────────────────────────────────────*/
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

    /*──────────────────────────────────────────────
     * 3.  UPDATE
     *─────────────────────────────────────────────*/
    @Operation(summary = "Aggiorna una prenotazione esistente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prenotazione aggiornata"),
            @ApiResponse(responseCode = "404", description = "Prenotazione non trovata"),
            @ApiResponse(responseCode = "409", description = "Transizione di stato non ammessa")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateHold(@PathVariable UUID id,
                                        @RequestBody HoldUpdateDto dto) {

        Optional<Hold> optionalHold = holdRepository.findById(id);
        if (optionalHold.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Hold existing = optionalHold.get();

        // non si può ri-attivare una prenotazione cancellata
        if (existing.getStatus() == HoldStatus.CANCELLED
            && dto.getStatus() != HoldStatus.CANCELLED) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", "Cannot change status from CANCELLED to another state"));
        }

        existing.setPickupBranch(dto.getPickupBranch());
        existing.setStatus(dto.getStatus());
        existing.setPosition(dto.getPosition());

        Hold updated = holdRepository.save(existing);
        return ResponseEntity.ok(holdMapper.toDto(updated));
    }

    /*──────────────────────────────────────────────
     * 4.  CANCEL (soft-delete)
     *─────────────────────────────────────────────*/
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

    /*──────────────────────────────────────────────
     * 5.  DELETE (hard)
     *─────────────────────────────────────────────*/
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

    /*──────────────────────────────────────────────
     * 6.  ALTRI END-POINT (libro associato, ricerca, dettagli…)
     *─────────────────────────────────────────────*/

    @GetMapping("/{id}/book")
    public ResponseEntity<BookDto> getBookForHold(@PathVariable UUID id) {
        return holdRepository.findById(id)
                .map(Hold::getBook)
                .map(bookMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Ricerca avanzata delle prenotazioni",
               description = "Filtra per titolo, autore, branch, status, genere, anno, posizione, e — se presenti — bibId+patronId")
    @GetMapping
    public ResponseEntity<List<HoldDto>> searchHolds(
            @RequestParam(required = false) String     title,
            @RequestParam(required = false) String     author,
            @RequestParam(required = false) String     pickupBranch,
            @RequestParam(required = false) HoldStatus status,
            @RequestParam(required = false) String     genre,
            @RequestParam(required = false) Integer    publicationYear,
            @RequestParam(required = false) Integer    position,
            @RequestParam(required = false) UUID       bibId,
            @RequestParam(required = false) UUID       patronId) {

        // compatibilità con regression_test.sh: se passano bibId+patronId
        if (bibId != null && patronId != null) {
            return holdRepository.findByPatronIdAndBibId(patronId, bibId)
                                 .map(h -> List.of(holdMapper.toDto(h)))
                                 .map(ResponseEntity::ok)
                                 .orElse(ResponseEntity.ok(List.of()));
        }

        List<Hold> results = holdRepository.searchByOptionalFilters(
                title, author, pickupBranch, status, genre, publicationYear, position);

        List<HoldDto> dto = results.stream()
                                   .map(holdMapper::toDto)
                                   .collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Dettagli completi (Hold + Book)")
    @GetMapping("/{id}/details")
    public ResponseEntity<HoldDetailsDto> getHoldDetails(@PathVariable UUID id) {
        return holdRepository.findByIdWithBook(id)
                .map(hold -> {
                    HoldDetailsDto d = new HoldDetailsDto();
                    d.setHold(holdMapper.toDto(hold));
                    d.setBook(bookMapper.toDto(hold.getBook()));
                    return ResponseEntity.ok(d);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /* endpoint legacy */
    @GetMapping("/search/find-by-title")
    public ResponseEntity<List<HoldDto>> legacyFindByTitle(@RequestParam String title) {
        var dto = holdRepository.findByBookTitleContainingIgnoreCase(title)
                                .stream()
                                .map(holdMapper::toDto)
                                .toList();
        return ResponseEntity.ok(dto);
    }
}
