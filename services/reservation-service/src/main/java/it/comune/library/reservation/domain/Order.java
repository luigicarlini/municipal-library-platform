package it.comune.library.reservation.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "🛒 Entità JPA dell’ordine di acquisto libro")
public class Order {

    @Id @GeneratedValue
    @Schema(description = "PK ordine", example = "42")
    private Long id;

    @Column(nullable = false)
    private UUID bookId;

    @Column(nullable = false)
    private Long patronId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceSnapshot;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    private String paymentReference;

    @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;

    /* hook automatici */
    @PrePersist void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }
    @PreUpdate  void onUpdate() { updatedAt = OffsetDateTime.now(); }
}
