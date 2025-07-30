package kr.hhplus.be.server.order.application.dto;

import kr.hhplus.be.server.order.domain.OrderItem;
import java.math.BigDecimal;

public record OrderItemResult(
    Long productId,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice
) {
    public static OrderItemResult from(OrderItem orderItem) {
        return new OrderItemResult(
            orderItem.getProductId(),
            orderItem.getQuantity(),
            orderItem.getProductPrice(),
            orderItem.getProductPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()))
        );
    }
}