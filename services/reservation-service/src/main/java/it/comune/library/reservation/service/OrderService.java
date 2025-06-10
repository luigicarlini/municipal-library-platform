package it.comune.library.reservation.service;

import it.comune.library.reservation.domain.Order;
import it.comune.library.reservation.domain.OrderStatus;
import it.comune.library.reservation.dto.OrderDto;
import it.comune.library.reservation.mapper.OrderMapper;
import it.comune.library.reservation.repository.BookRepository;
import it.comune.library.reservation.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 💼 Logica di business per gli ordini Bookshop.
 */
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository,
                        BookRepository bookRepository,
                        OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
        this.orderMapper = orderMapper;
    }

    /* ---------- CRUD & flussi principali ---------- */

    /**
     * Crea un nuovo ordine (status = CREATED) e calcola i prezzi a partire
     * dal libro corrente.
     */
    public OrderDto create(OrderDto dto) {
        // 1. Validazione libro
        var book = bookRepository.findById(dto.getBookId())
                                 .orElseThrow(() ->
                                      new IllegalArgumentException("Book not found: " + dto.getBookId()));

        // 2. Costruzione ordine
        Order order = Order.builder()
                .bookId(dto.getBookId())
                .patronId(dto.getPatronId())
                .quantity(dto.getQuantity())
                .unitPriceSnapshot(book.getPrice())                // copia prezzo
                .totalPrice(book.getPrice().multiply(
                        BigDecimal.valueOf(dto.getQuantity())))
                .status(OrderStatus.CREATED)
                .build();

        // 3. Persistenza
        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    /** Recupera un ordine per id. */
    public Optional<OrderDto> get(Long id) {
        return orderRepository.findById(id).map(orderMapper::toDto);
    }

    /** Lista completa ordini. */
    public List<OrderDto> findAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toDto)
                .toList();
    }

    /**
     * Annulla un ordine finché non è pagato.
     *
     * @return true se trovato e annullato
     */
    public boolean cancel(Long id) {
        return orderRepository.findById(id).map(order -> {
            if (order.getStatus() == OrderStatus.CREATED ||
                order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                return true;
            }
            return false;
        }).orElse(false);
    }

    /**
     * Marca un ordine come pagato (mock webhook).
     *
     * @return true se aggiornato, false se non permesso o non trovato
     */
    public boolean markPaid(Long id, String gatewayRef) {
        return orderRepository.findById(id).map(order -> {

            // ❌ se è già CANCELLED o PAID → non consentito
            if (order.getStatus() == OrderStatus.CANCELLED ||
                    order.getStatus() == OrderStatus.PAID) {
                return false;
            }

            order.setStatus(OrderStatus.PAID);
            order.setPaymentReference(gatewayRef);
            orderRepository.save(order);
            return true;

        }).orElse(false);
    }
}
