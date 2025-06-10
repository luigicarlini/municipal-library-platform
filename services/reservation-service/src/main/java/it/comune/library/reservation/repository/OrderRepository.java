package it.comune.library.reservation.repository;

import it.comune.library.reservation.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository JPA per l’entità {@link Order}.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
    // per ora non servono query custom: ereditiamo CRUD completo da JpaRepository
}

