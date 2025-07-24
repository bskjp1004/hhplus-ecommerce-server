package kr.hhplus.be.server.order.infra.entity;

import kr.hhplus.be.server.order.domain.Order;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;

@Entity
@AllArgsConstructor
public class OrderEntity {
    private final long id;
    private final long userId;
    private final long couponId;
    private final LocalDateTime orderedAt;
    private final BigDecimal totalPrice;
    private final BigDecimal discountRate;
    private final BigDecimal paidPrice;
    private final List<OrderItemEntity> orderItems;

    public Order toDomain(){
        return Order.builder()
            .id(this.id)
            .userId(this.userId)
            .couponId(this.couponId)
            .orderedAt(this.orderedAt)
            .totalPrice(this.totalPrice)
            .discountRate(this.discountRate)
            .paidPrice(this.paidPrice)
            .orderItems(this.orderItems.stream()
                .map(OrderItemEntity::toDomain)
                .toList())
            .build();
    }

    public static OrderEntity fromDomain(Order order){
        return new OrderEntity(
            order.getId(),
            order.getUserId(),
            order.getCouponId(),
            order.getOrderedAt(),
            order.getTotalPrice(),
            order.getDiscountRate(),
            order.getPaidPrice(),
            order.getOrderItems()
                .stream()
                .map(OrderItemEntity::fromDomain)
                .toList()
        );
    }
}
