package kr.hhplus.be.server.order.application.dto;

import java.util.List;

public record CreateOrderCommand(
    Long userId,
    Long couponId,
    List<OrderItemCommand> OrderItemCommands
) {
}