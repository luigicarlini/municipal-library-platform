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

import it.comune.library.reservation.exception.InsufficientStockException;

/* ---------- imports AGGIUNTIVI ---------- */
// import it.comune.library.reservation.exception.InsufficientStockException;
// import it.comune.library.reservation.domain.Book;
import jakarta.persistence.EntityNotFoundException;
// import jakarta.transaction.Transactional;
// import org.springframework.data.jpa.repository.Lock;
// import jakarta.persistence.LockModeType;

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

    /**
     * Crea un nuovo ordine (status = CREATED) e calcola i prezzi a partire
     * dal libro corrente.<br>
     * <ul>
     * <li>Valida l’esistenza del libro</li>
     * <li>Verifica che la quantità richiesta non superi lo stock disponibile</li>
     * <li>Calcola <code>unitPriceSnapshot</code> e <code>totalPrice</code></li>
     * </ul>
     *
     * @throws InsufficientStockException se
     *                                    <code>quantity &gt; stockQuantity</code>
     */
    public OrderDto create(OrderDto dto) {

        /* 1) Validazione libro */
        var book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + dto.getBookId()));

        /* 2) Verifica stock sufficiente */
        if (dto.getQuantity() > book.getStockQuantity()) {
            throw new InsufficientStockException(
                    "Requested quantity (" + dto.getQuantity() +
                            ") exceeds stock (" + book.getStockQuantity() + ")");
        }

        /* 3) Costruzione ordine */
        Order order = Order.builder()
                .bookId(dto.getBookId())
                .patronId(dto.getPatronId())
                .quantity(dto.getQuantity())
                .unitPriceSnapshot(book.getPrice()) // copia prezzo al momento dell’ordine
                .totalPrice(book.getPrice().multiply(
                        BigDecimal.valueOf(dto.getQuantity())))
                .status(OrderStatus.CREATED)
                .build();

        /* 4) Persistenza */
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
     * Callback (mock) del gateway di pagamento.
     * <p>
     * • Accetta un ordine ancora «aperto» (<code>CREATED</code> o
     * <code>PENDING</code>) e lo marca come <code>PAID</code>.<br>
     * • Decrementa lo stock in modo <strong>atomico</strong> tramite
     * l’UPDATE condizionato definito in
     * {@link BookRepository#decrementStockIfEnough}.
     *
     * @param id         id ordine
     * @param gatewayRef riferimento/receipt restituito dal gateway
     *
     * @return <code>true</code> se la transizione è avvenuta;<br>
     *         <code>false</code> se l’ordine era già definitivo
     *
     * @throws InsufficientStockException se lo stock non è sufficiente
     */
    @Transactional
    public boolean markPaid(Long id, String gatewayRef) {

        /* 1️⃣ carica ordine (EntityNotFound → 404 nel RestExceptionAdvice) */
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order " + id));

        /* 2️⃣ stato già definitivo → nessuna azione, niente eccezioni */
        if (order.getStatus() == OrderStatus.PAID ||
                order.getStatus() == OrderStatus.CANCELLED) {
            return false;
        }

        /*
         * 3️⃣ UPDATE atomico: scala lo stock solo se sufficiente *
         * BookRepository.decrementStockIfEnough(...) restituisce il numero *
         * di righe aggiornate (0 → stock insufficiente).
         */
        int rows = bookRepository.decrementStockIfEnough(
                order.getBookId(), // UUID libro
                order.getQuantity()); // qty da scalare

        if (rows == 0) {
            /* nessuna riga toccata ⇒ stock esaurito o insufficiente */
            throw new InsufficientStockException(
                    "Insufficient stock for book " + order.getBookId());
        }

        /*
         * 4️⃣ aggiorna l’ordine *
         * (entità già «managed»; il flush esplicito assicura che il test *
         * legga subito lo stato PAID)
         */
        order.setStatus(OrderStatus.PAID);
        order.setPaymentReference(gatewayRef);
        orderRepository.saveAndFlush(order);

        return true;
    }
}
