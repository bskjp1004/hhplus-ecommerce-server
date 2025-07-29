package kr.hhplus.be.server.order.controller.dto;

public record OrderItemRequest (
        Long productId,
        Integer quantity
) {
}

