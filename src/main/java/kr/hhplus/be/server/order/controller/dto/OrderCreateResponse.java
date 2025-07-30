package kr.hhplus.be.server.order.controller.dto;

import kr.hhplus.be.server.order.application.dto.OrderResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreateResponse(
    Long orderId,
    Long userId,
    BigDecimal totalPrice,
    BigDecimal paidPrice,
    LocalDateTime orderedAt,
    List<OrderItemResponse> orderItems
) {
    public record OrderItemResponse(
        Long productId,
        Integer quantity
    ) {}
    
    public static OrderCreateResponse from(OrderResult result) {
        return new OrderCreateResponse(
            result.id(),
            result.userId(),
            result.totalPrice(),
            result.paidPrice(),
            result.orderedAt(),
            result.orderItems().stream()
                .map(item -> new OrderItemResponse(
                    item.productId(),
                    item.quantity()
                ))
                .toList()
        );
    }
}