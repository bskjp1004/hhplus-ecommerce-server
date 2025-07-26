package kr.hhplus.be.server.order.application.dto;

import kr.hhplus.be.server.order.domain.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponseDto(
    long id,
    long orderId,
    long productId,
    Integer quantity,
    BigDecimal productPrice
) {
    public static OrderItemResponseDto from(OrderItem orderItem){
        return new OrderItemResponseDto(
            orderItem.getId(),
            orderItem.getOrderId(),
            orderItem.getProductId(),
            orderItem.getQuantity(),
            orderItem.getProductPrice()
        );
    }
}
