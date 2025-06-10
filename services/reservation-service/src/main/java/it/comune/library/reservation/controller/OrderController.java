package it.comune.library.reservation.controller;

import it.comune.library.reservation.dto.OrderDto;
import it.comune.library.reservation.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🎯 Controller REST per la gestione degli ordini di acquisto libri.
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "OrderController", description = "Gestione degli ordini (Bookshop)")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /* ---------- CRUD base ---------- */

    @Operation(summary = "Crea un nuovo ordine")
    @ApiResponse(responseCode = "201", description = "Ordine creato con successo")
    @PostMapping
    public ResponseEntity<OrderDto> create(@RequestBody OrderDto dto) {
        OrderDto saved = orderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Recupera un ordine per ID")
    @ApiResponse(responseCode = "200", description = "Ordine trovato")
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@PathVariable Long id) {
        return orderService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lista tutti gli ordini")
    @ApiResponse(responseCode = "200", description = "Lista restituita con successo")
    @GetMapping
    public ResponseEntity<List<OrderDto>> list() {
        return ResponseEntity.ok(orderService.findAll());
    }

    /* ---------- Comandi di dominio ---------- */

    @Operation(summary = "Annulla un ordine finché non è pagato (soft-delete)")
    @ApiResponse(responseCode = "204", description = "Ordine annullato")
    @PutMapping("/{id}/cancel")                          // ★ era DELETE
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        boolean done = orderService.cancel(id);
        return done ? ResponseEntity.noContent().build()
                    : ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 se non annullabile
    }

    @Operation(summary = "Marca un ordine come pagato (mock webhook)")
    @ApiResponse(responseCode = "204", description = "Stato aggiornato")
    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<Void> markPaid(@PathVariable Long id,
            @RequestParam String gatewayRef) {
        boolean done = orderService.markPaid(id, gatewayRef);
        return done ? ResponseEntity.noContent().build()
                : ResponseEntity.status(
                        orderService.get(id).isPresent()
                                ? HttpStatus.CONFLICT // esiste ma stato non valido
                                : HttpStatus.NOT_FOUND // non esiste
                ).build();
    }
}