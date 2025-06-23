package it.comune.library.reservation.controller;

import it.comune.library.reservation.domain.Hold;
import it.comune.library.reservation.domain.HoldStatus;
import it.comune.library.reservation.dto.*;
import it.comune.library.reservation.mapper.BookMapper;
import it.comune.library.reservation.mapper.HoldMapper;
import it.comune.library.reservation.repository.BookRepository;
import it.comune.library.reservation.repository.HoldRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.PageRequest;
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

    /*──────────────────────────────────────────────
     * 1. CREATE
     *─────────────────────────────────────────────*/
    @Operation(summary = "Crea una nuova prenotazione")
    @PostMapping
    public ResponseEntity<?> createHold(@RequestBody HoldDto dto) {

        if (!bookRepository.existsById(dto.getBibId())) {
            return ResponseEntity.status(409).body(
                Map.of("error", "Book not available for bibId: " + dto.getBibId()));
        }

        if (holdRepository.findByPatronIdAndBibId(dto.getPatronId(), dto.getBibId()).isPresent()) {
            return ResponseEntity.status(409).body(
                Map.of("error", "Duplicate hold: patron already requested this book"));
        }

        Hold hold = holdMapper.toEntity(dto);
        hold.setId(UUID.randomUUID());
        hold.setStatus(HoldStatus.PLACED);
        hold.setPosition(holdRepository.findMaxPositionByBibId(dto.getBibId()) + 1);

        return ResponseEntity.ok(holdMapper.toDto(holdRepository.save(hold)));
    }

    /*──────────────────────────────────────────────
     * 2. READ
     *─────────────────────────────────────────────*/
    @GetMapping("/{id}")
    public ResponseEntity<HoldDto> getHoldById(@PathVariable UUID id) {
        return holdRepository.findById(id)
                .map(hold -> ResponseEntity.ok(holdMapper.toDto(hold)))
                .orElse(ResponseEntity.notFound().build());
    }

    /*──────────────────────────────────────────────
     * 3. UPDATE
     *─────────────────────────────────────────────*/
    @PutMapping("/{id}")
    public ResponseEntity<?> updateHold(@PathVariable UUID id,
                                        @RequestBody HoldUpdateDto dto) {

        return holdRepository.findById(id).map(existing -> {

            if (existing.getStatus() == HoldStatus.CANCELLED &&
                dto.getStatus() != HoldStatus.CANCELLED) {
                return ResponseEntity.status(409).body(
                    Map.of("error", "Cannot change status from CANCELLED"));
            }
            existing.setPickupBranch(dto.getPickupBranch());
            existing.setStatus(dto.getStatus());
            existing.setPosition(dto.getPosition());
            return ResponseEntity.ok(holdMapper.toDto(holdRepository.save(existing)));

        }).orElse(ResponseEntity.notFound().build());
    }

    /*──────────────────────────────────────────────
     * 4. CANCEL / DELETE
     *─────────────────────────────────────────────*/
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelHold(@PathVariable UUID id) {
        return holdRepository.findById(id).map(h -> {
            h.setStatus(HoldStatus.CANCELLED);
            holdRepository.save(h);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHold(@PathVariable UUID id) {
        if (!holdRepository.existsById(id)) return ResponseEntity.notFound().build();
        holdRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /*──────────────────────────────────────────────
     * 5. BOOK FOR HOLD
     *─────────────────────────────────────────────*/
    @GetMapping("/{id}/book")
    public ResponseEntity<BookDto> getBookForHold(@PathVariable UUID id) {
        return holdRepository.findById(id)
                .map(Hold::getBook)
                .map(bookMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /*──────────────────────────────────────────────
     * 6. SEARCH paginata
     *─────────────────────────────────────────────*/
    @Operation(summary = "Ricerca avanzata (paginata) delle prenotazioni")
    @GetMapping
    public ResponseEntity<List<HoldDto>> searchHolds(
            @RequestParam(required = false) String     title,
            @RequestParam(required = false) String     author,
            @RequestParam(required = false) String     pickupBranch,
            @RequestParam(required = false) HoldStatus status,
            @RequestParam(required = false) String     genre,
            @RequestParam(required = false) Integer    publicationYear,
            @Parameter(description = "Numero pagina (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Dimensione pagina", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID       bibId,
            @RequestParam(required = false) UUID       patronId) {

        if (bibId != null && patronId != null) {
            return holdRepository.findByPatronIdAndBibId(patronId, bibId)
                                 .map(h -> List.of(holdMapper.toDto(h)))
                                 .map(ResponseEntity::ok)
                                 .orElse(ResponseEntity.ok(List.of()));
        }

        var pageable   = PageRequest.of(page, size);
        var pageResult = holdRepository.searchPaged(
                title, author, pickupBranch, status, genre, publicationYear, pageable);

        var dto = pageResult.getContent().stream()
                            .map(holdMapper::toDto)
                            .collect(Collectors.toList());

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(pageResult.getTotalElements()))
                .body(dto);
    }

    /*──────────────────────────────────────────────
     * 7. DETAILS
     *─────────────────────────────────────────────*/
    @GetMapping("/{id}/details")
    public ResponseEntity<HoldDetailsDto> getHoldDetails(@PathVariable UUID id) {
        return holdRepository.findByIdWithBook(id).map(hold -> {
            HoldDetailsDto d = new HoldDetailsDto();
            d.setHold(holdMapper.toDto(hold));
            d.setBook(bookMapper.toDto(hold.getBook()));
            return ResponseEntity.ok(d);
        }).orElse(ResponseEntity.notFound().build());
    }

    /*──────────────────────────────────────────────
     * 8. LEGACY alias
     *─────────────────────────────────────────────*/
    @GetMapping("/search/find-by-title")
    public ResponseEntity<List<HoldDto>> legacyFindByTitle(@RequestParam String title) {
        return ResponseEntity.ok(
                holdRepository.findByBookTitleContainingIgnoreCase(title)
                              .stream()
                              .map(holdMapper::toDto)
                              .toList());
    }
}