package kr.hhplus.be.server.order.controller.dto;

import java.util.List;

public record OrderCreateRequest(
    Long userId,
    Long couponId,
    List<OrderItemRequest> orderItems
) {

}