package kr.hhplus.be.server.order.application;

import kr.hhplus.be.server.order.application.dto.OrderRequestDto;
import kr.hhplus.be.server.order.application.dto.OrderResponseDto;

public interface OrderFacade {
    OrderResponseDto placeOrderWithPayment(OrderRequestDto orderRequestDto);
}
