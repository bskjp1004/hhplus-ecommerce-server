package kr.hhplus.be.server.order.application.dto;

import java.math.BigDecimal;
import kr.hhplus.be.server.order.domain.OrderItem;

public record OrderItemRequestDto (
    long productId,
    Integer quantity
) {
    public OrderItem toDomain(){
        return OrderItem.builder()
            .productId(this.productId)
            .quantity(this.quantity)
            .build();
    }
}
