package kr.hhplus.be.server.order.application.dto;

public record OrderItemCommand(
    Long productId,
    Integer quantity
) {}