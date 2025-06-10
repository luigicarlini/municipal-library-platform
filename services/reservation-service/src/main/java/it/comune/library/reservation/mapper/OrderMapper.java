package it.comune.library.reservation.mapper;

import it.comune.library.reservation.domain.Order;
import it.comune.library.reservation.domain.OrderStatus;
import it.comune.library.reservation.dto.OrderDto;
import org.springframework.stereotype.Component;

/**
 * Mapper manuale tra {@link Order} (domain) e {@link OrderDto} (API).
 */
@Component
public class OrderMapper {

    /* ===== Entity ➜ DTO ===== */

    public OrderDto toDto(Order o) {
        OrderDto d = new OrderDto();
        d.setId(o.getId());
        d.setBookId(o.getBookId());
        d.setPatronId(o.getPatronId());
        d.setQuantity(o.getQuantity());
        d.setTotalPrice(o.getTotalPrice());

        // enum -> String
        d.setStatus(o.getStatus() != null ? o.getStatus().name() : null);

        d.setCreatedAt(o.getCreatedAt());
        return d;
    }

    /* ===== DTO ➜ Entity ===== */

    public Order toEntity(OrderDto d) {
        return Order.builder()
                .id(d.getId())
                .bookId(d.getBookId())
                .patronId(d.getPatronId())
                .quantity(d.getQuantity())
                .totalPrice(d.getTotalPrice())

                // String -> enum
                .status(d.getStatus() != null ? OrderStatus.valueOf(d.getStatus()) : null)

                .build();
    }
}
