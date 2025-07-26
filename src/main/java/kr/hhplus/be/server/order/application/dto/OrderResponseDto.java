package kr.hhplus.be.server.order.application.dto;

import kr.hhplus.be.server.order.domain.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
    long id,
    long userId,
    long couponId,
    LocalDateTime orderedAt,
    BigDecimal totalPrice,
    BigDecimal discountRate,
    BigDecimal paidPrice,
    List<OrderItemResponseDto>orderItems
) {
    public static OrderResponseDto from(Order order){
        return new OrderResponseDto(
            order.getId(),
            order.getUserId(),
            order.getCouponId(),
            order.getOrderedAt(),
            order.getTotalPrice(),
            order.getDiscountRate(),
            order.getPaidPrice(),
            order.getOrderItems().stream()
                .map(OrderItemResponseDto::from)
                .toList()
        );
    }
}
