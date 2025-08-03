package kr.hhplus.be.server.order.infra.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import kr.hhplus.be.server.order.domain.Order;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "`order`")
@Getter
@Setter
@NoArgsConstructor()
@AllArgsConstructor
@Builder
public class OrderJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = true)
    private Long couponId;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private BigDecimal discountRate;

    @Column(nullable = false)
    private BigDecimal paidPrice;

    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<OrderItemJpaEntity> orderItems = new ArrayList<>();

    public Order toDomain() {
        return Order.builder()
            .id(this.id)
            .userId(this.userId)
            .couponId(this.couponId)
            .orderedAt(this.orderedAt)
            .totalPrice(this.totalPrice)
            .discountRate(this.discountRate)
            .paidPrice(this.paidPrice)
            .orderItems(this.orderItems.stream()
                .map(OrderItemJpaEntity::toDomain)
                .toList())
            .build();
    }

    public static OrderJpaEntity fromDomain(Order order) {
        OrderJpaEntity entity = OrderJpaEntity.builder()
            .id(order.getId() != null && order.getId() > 0 ? order.getId() : null)
            .userId(order.getUserId())
            .couponId(order.getCouponId())
            .orderedAt(order.getOrderedAt())
            .totalPrice(order.getTotalPrice())
            .discountRate(order.getDiscountRate())
            .paidPrice(order.getPaidPrice())
            .build();

        order.getOrderItems().forEach(item -> {
            OrderItemJpaEntity itemEntity = OrderItemJpaEntity.fromDomain(item);
            itemEntity.setOrder(entity);
            entity.getOrderItems().add(itemEntity);
        });

        return entity;
    }
}
