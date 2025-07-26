package kr.hhplus.be.server.order.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import kr.hhplus.be.server.order.domain.exception.OrderDomainException;
import lombok.Builder;
import lombok.Getter;

@Getter

public class Order {
    private final long id;
    private final long userId;
    private final long couponId;
    private final LocalDateTime orderedAt;
    private final BigDecimal totalPrice;
    private final BigDecimal discountRate;
    private final BigDecimal paidPrice;
    private final List<OrderItem> orderItems;

    @Builder
    private Order(long id, long userId, long couponId,
        LocalDateTime orderedAt, BigDecimal totalPrice,
        BigDecimal discountRate, BigDecimal paidPrice, List<OrderItem> orderItems){
        this.id = id;
        this.userId = userId;
        this.couponId = couponId;
        this.orderedAt = orderedAt;
        this.totalPrice = totalPrice;
        this.discountRate = discountRate;
        this.paidPrice = paidPrice;
        this.orderItems = orderItems;
    }

    public static Order create(long userId, long couponId, BigDecimal discountRate, List<OrderItem> orderItems){
        if (orderItems == null || orderItems.isEmpty()){
            throw new OrderDomainException.EmptyOrderItemsException();
        }

        LocalDateTime nowDateTime = LocalDateTime.now();

        BigDecimal totalPrice = orderItems.stream()
            .map(OrderItem::calculatePrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidPrice = totalPrice.multiply(BigDecimal.ONE.subtract(discountRate));

        return Order.builder()
            .userId(userId)
            .couponId(couponId)
            .orderedAt(nowDateTime)
            .totalPrice(totalPrice)
            .discountRate(discountRate)
            .paidPrice(paidPrice)
            .orderItems(orderItems)
            .build();
    }
}
