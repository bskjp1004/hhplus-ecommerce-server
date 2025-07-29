package kr.hhplus.be.server.order.application.dto;

import kr.hhplus.be.server.order.domain.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResult(
    long id,
    long userId,
    long couponId,
    LocalDateTime orderedAt,
    BigDecimal totalPrice,
    BigDecimal discountRate,
    BigDecimal paidPrice,
    List<OrderItemResult>orderItems
) {
    public static OrderResult from(Order order){
        return new OrderResult(
            order.getId(),
            order.getUserId(),
            order.getCouponId(),
            order.getOrderedAt(),
            order.getTotalPrice(),
            order.getDiscountRate(),
            order.getPaidPrice(),
            order.getOrderItems().stream()
                .map(OrderItemResult::from)
                .toList()
        );
    }
}
