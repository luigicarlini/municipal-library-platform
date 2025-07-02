package it.comune.library.reservation.controller;

import it.comune.library.reservation.domain.HoldStatus;
import it.comune.library.reservation.dto.*;
import it.comune.library.reservation.mapper.BookMapper;
import it.comune.library.reservation.mapper.HoldMapper;
import it.comune.library.reservation.service.HoldService;
import it.comune.library.reservation.repository.BookRepository;
import it.comune.library.reservation.repository.HoldRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.headers.Header;   //  ← nuovo import

@RestController
@RequestMapping("/holds")
@Tag(name = "HoldController", description = "Hold management (create / read / update / cancel / search)")
@RequiredArgsConstructor
public class HoldController {

    /* ------------------------------------------------------------------ */
    private final HoldService holdService;
    private final HoldRepository holdRepository; // - legacy / util
    private final BookRepository bookRepository; // - per validazioni create
    private final HoldMapper holdMapper;
    private final BookMapper bookMapper;
    /* ------------------------------------------------------------------ */

    /*
     * ██████████████████████████████████████████████████████████████████
     * 1. CREATE
     * ██████████████████████████████████████████████████████████████████
     */
    @Operation(summary = "Create a new hold", description = "Creates a hold for a given patron/book pair. Fails with **409** if "
            + "the book does not exist or the patron already placed a hold for the same book.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hold created", content = @Content(schema = @Schema(implementation = HoldDto.class))),
            @ApiResponse(responseCode = "409", description = "Book not available or duplicate hold", content = @Content(schema = @Schema(example = "{\"error\":\"…\"}")))
    })
    @PostMapping
    public ResponseEntity<?> createHold(@RequestBody HoldDto dto) {

        /* quick validations */
        if (!bookRepository.existsById(dto.getBibId())) {
            return ResponseEntity.status(409).body(
                    Map.of("error", "Book not available for bibId: " + dto.getBibId()));
        }
        if (holdRepository.findByPatronIdAndBibId(dto.getPatronId(), dto.getBibId()).isPresent()) {
            return ResponseEntity.status(409).body(
                    Map.of("error", "Duplicate hold: patron already requested this book"));
        }

        HoldDto created = holdService.create(dto);
        return ResponseEntity.ok(created);
    }

    /*
     * ██████████████████████████████████████████████████████████████████
     * 2. READ - by ID
     * ██████████████████████████████████████████████████████████████████
     */
    @Operation(summary = "Get hold by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hold found", content = @Content(schema = @Schema(implementation = HoldDto.class))),
            @ApiResponse(responseCode = "404", description = "Hold not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<HoldDto> getHoldById(@PathVariable UUID id) {
        return holdRepository.findById(id)
                .map(holdMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /*
     * ██████████████████████████████████████████████████████████████████
     * 3. UPDATE (parziale)
     * ██████████████████████████████████████████████████████████████████
     */
    @Operation(summary = "Update an existing hold", description = "Allows changing **pickupBranch**, **status** and **position**. "
            + "Status cannot be changed once the hold is *CANCELLED*.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hold updated", content = @Content(schema = @Schema(implementation = HoldDto.class))),
            @ApiResponse(responseCode = "404", description = "Hold not found"),
            @ApiResponse(responseCode = "409", description = "Invalid state transition", content = @Content(schema = @Schema(example = "{\"error\":\"…\"}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateHold(@PathVariable UUID id,
            @RequestBody HoldUpdateDto dto) {

        return holdRepository.findById(id).map(existing -> {
            /* semplice business-rule: non riattivare CANCELLED */
            if (existing.getStatus() == HoldStatus.CANCELLED &&
                    dto.getStatus() != HoldStatus.CANCELLED) {
                return ResponseEntity.status(409)
                        .body(Map.of("error", "Cannot change status from CANCELLED"));
            }
            existing.setPickupBranch(dto.getPickupBranch());
            existing.setStatus(dto.getStatus());
            existing.setPosition(dto.getPosition());
            return ResponseEntity.ok(holdMapper.toDto(holdRepository.save(existing)));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /*
     * ██████████████████████████████████████████████████████████████████
     * 4. CANCEL & DELETE
     * ██████████████████████████████████████████████████████████████████
     */
    @Operation(summary = "Soft-cancel a hold")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hold cancelled"),
            @ApiResponse(responseCode = "404", description = "Hold not found")
    })
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelHold(@PathVariable UUID id) {
        return holdRepository.findById(id).map(h -> {
            h.setStatus(HoldStatus.CANCELLED);
            holdRepository.save(h);
            return ResponseEntity.noContent().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Hard-delete a hold")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hold deleted"),
            @ApiResponse(responseCode = "404", description = "Hold not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHold(@PathVariable UUID id) {
        if (!holdRepository.existsById(id))
            return ResponseEntity.notFound().build();
        holdRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /*
     * ██████████████████████████████████████████████████████████████████
     * 5. BOOK linked to HOLD
     * ██████████████████████████████████████████████████████████████████
     */
    @Operation(summary = "Get the book linked to a hold")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found", content = @Content(schema = @Schema(implementation = BookDto.class))),
            @ApiResponse(responseCode = "404", description = "Hold not found")
    })
    @GetMapping("/{id}/book")
    public ResponseEntity<BookDto> getBookForHold(@PathVariable UUID id) {
        return holdRepository.findById(id)
                .map(hold -> bookMapper.toDto(hold.getBook()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /*
     * ██████████████████████████████████████████████████████████████████
     * 6. SEARCH paginata (status + branch)
     * ██████████████████████████████████████████████████████████████████
     */
    @Operation(summary = "Paginated hold search", description = """
            Returns a **paginated list** of holds filtered by:
            • `status` – hold state
            • `pickup_branch` – branch code (case-insensitive)

            Sorting is available via Spring’s `?sort=field,(asc|desc)` syntax.
            The total number of matching records is returned in the **X-Total-Count** response header.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list", headers = @Header(name = "X-Total-Count", description = "Total matching records", schema = @Schema(type = "integer", example = "42")), content = @Content(schema = @Schema(implementation = HoldDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<HoldDto>> searchPaged(
            @Parameter(description = "Filter by hold status") @RequestParam(required = false) HoldStatus status,

            @Parameter(description = "Filter by pickup branch", example = "CENTRAL") @RequestParam(required = false, name = "pickup_branch") String branch,

            @Parameter(description = "Page index (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) Pageable pageable) { // per sort
        Page<HoldDto> result = holdService.searchPaged(
                status,
                branch,
                page,
                size,
                pageable.getSort());

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .body(result.getContent());
    }

    /*
     * ██████████████████████████████████████████████████████████████████
     * 7. DETAILS (Hold + Book DTO)
     * ██████████████████████████████████████████████████████████████████
     */
    @Operation(summary = "Hold details (hold + book)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Details found", content = @Content(schema = @Schema(implementation = HoldDetailsDto.class))),
            @ApiResponse(responseCode = "404", description = "Hold not found")
    })
    @GetMapping("/{id}/details")
    public ResponseEntity<HoldDetailsDto> getHoldDetails(@PathVariable UUID id) {
        return holdRepository.findByIdWithBook(id).map(hold -> {
            HoldDetailsDto d = new HoldDetailsDto();
            d.setHold(holdMapper.toDto(hold));
            d.setBook(bookMapper.toDto(hold.getBook()));
            return ResponseEntity.ok(d);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /*
     * ██████████████████████████████████████████████████████████████████
     * 8. LEGACY - ricerca per titolo (no paging)
     * ██████████████████████████████████████████████████████████████████
     */
    @Operation(summary = "Legacy search by book title", description = "Non-paginated endpoint kept for backward compatibility.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of holds", content = @Content(schema = @Schema(implementation = HoldDto.class)))
    })
    @GetMapping("/search/find-by-title")
    public ResponseEntity<List<HoldDto>> legacyFindByTitle(@RequestParam String title) {
        return ResponseEntity.ok(
                holdRepository.findByBookTitleContainingIgnoreCase(title).stream()
                        .map(holdMapper::toDto)
                        .collect(Collectors.toList()));
    }
}
