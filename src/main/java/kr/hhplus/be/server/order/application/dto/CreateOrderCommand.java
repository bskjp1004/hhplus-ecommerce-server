package kr.hhplus.be.server.order.application.dto;

import java.util.List;
import org.springframework.lang.Nullable;

public record CreateOrderCommand(
    Long userId,
    @Nullable
    Long couponId,
    List<OrderItemCommand> OrderItemCommands
) {
}