package it.comune.library.reservation.controller;

import it.comune.library.reservation.dto.OrderDto;
import it.comune.library.reservation.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎯 REST-controller per la gestione degli ordini (Book-shop).
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "Orders (Book-shop)", description = "Crea, annulla, paga ordini di libri")
public class OrderController {

    private final OrderService svc;

    public OrderController(OrderService svc) { this.svc = svc; }

    /* ------------------------------------------------------------------ */
    /* CRUD                                                                */
    /* ------------------------------------------------------------------ */

    @Operation(
        summary     = "Crea un nuovo ordine",
        description = "Lo stato iniziale è <code>CREATED</code>."
    )
    @ApiResponse(
        responseCode = "201",
        description  = "Ordine creato",
        content      = @Content(schema = @Schema(implementation = OrderDto.class))
    )
    @PostMapping
    @Transactional
    public ResponseEntity<OrderDto> create(@RequestBody OrderDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(svc.create(dto));
    }

    @Operation(summary = "Recupera un ordine")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ordine trovato"),
        @ApiResponse(responseCode = "404", description = "Ordine non esistente")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> get(@PathVariable Long id) {
        return svc.get(id).map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lista tutti gli ordini")
    @ApiResponse(responseCode = "200", description = "Lista completa")
    @GetMapping
    public List<OrderDto> list() { return svc.findAll(); }

    /* ------------------------------------------------------------------ */
    /* /cancel  – soft-delete prima del pagamento                         */
    /* ------------------------------------------------------------------ */

    @Operation(
        summary = "Annulla un ordine **non pagato**",
        description =
            """
            • Stato ammesso: <code>CREATED</code> / <code>PENDING</code><br>
            • Se l’ordine è già <code>PAID</code> o <code>CANCELLED</code>
              viene restituito <code>409 CONFLICT</code>.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Ordine annullato"),
        @ApiResponse(responseCode = "404", description = "Ordine non esistente"),
        @ApiResponse(responseCode = "409", description = "Stato non annullabile")
    })
    @PutMapping("/{id}/cancel")
    @Transactional
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        /* true  = annullato
           false = ordine assente  **oppure** stato già definitivo        */
        boolean ok = svc.cancel(id);

        if (ok) return ResponseEntity.noContent().build();
        return svc.get(id).isPresent()
               ? ResponseEntity.status(HttpStatus.CONFLICT).build()
               : ResponseEntity.notFound().build();
    }

    /* ------------------------------------------------------------------ */
    /* /mark-paid – callback (mock) del gateway di pagamento              */
    /* ------------------------------------------------------------------ */

    @Operation(
        summary = "Marca un ordine come pagato",
        parameters = @Parameter(
            name        = "gatewayRef",
            in          = ParameterIn.QUERY,
            description = "Riferimento univoco del pagamento",
            example     = "PAY-123456789"
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Ordine marcato come pagato"),
        @ApiResponse(responseCode = "404", description = "Ordine non esistente"),
        @ApiResponse(responseCode = "409", description = "Ordine già pagato / annullato")
    })
    @PutMapping("/{id}/mark-paid")
    @Transactional
    public ResponseEntity<Void> markPaid(@PathVariable Long id,
                                         @RequestParam String gatewayRef) {

        boolean ok = svc.markPaid(id, gatewayRef);

        if (ok) return ResponseEntity.noContent().build();
        return svc.get(id).isPresent()
               ? ResponseEntity.status(HttpStatus.CONFLICT).build()
               : ResponseEntity.notFound().build();
    }
}
