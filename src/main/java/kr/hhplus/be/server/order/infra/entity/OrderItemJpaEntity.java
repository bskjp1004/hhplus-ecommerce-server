package kr.hhplus.be.server.order.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import kr.hhplus.be.server.order.domain.OrderItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor()
@AllArgsConstructor
@Builder
public class OrderItemJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Column(nullable = false)
    private long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal productPrice;

    public OrderItem toDomain(){
        return OrderItem.builder()
            .id(this.id)
            .orderId(this.order.getId())
            .productId(this.productId)
            .quantity(this.quantity)
            .productPrice(this.productPrice)
            .build();
    }

    public static OrderItemJpaEntity fromDomain(OrderItem item) {
        return OrderItemJpaEntity.builder()
            .id(item.getId() > 0 ? item.getId() : null)
            .productId(item.getProductId())
            .quantity(item.getQuantity())
            .productPrice(item.getProductPrice())
            .build();
    }
}
