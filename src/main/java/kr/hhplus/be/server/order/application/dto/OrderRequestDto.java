package kr.hhplus.be.server.order.application.dto;

import java.util.List;

public record OrderRequestDto(
    long userId,
    long couponId,
    List<OrderItemRequestDto> orderItems
) {
}
