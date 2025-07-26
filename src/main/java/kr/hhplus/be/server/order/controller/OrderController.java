package kr.hhplus.be.server.order.controller;

import kr.hhplus.be.server.order.application.OrderFacade;
import kr.hhplus.be.server.order.application.dto.OrderRequestDto;
import kr.hhplus.be.server.order.application.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderFacade orderFacade;

    @PostMapping("/order")
    public ResponseEntity<OrderResponseDto> getProduct(
        @RequestBody OrderRequestDto requestDto
    ) {
        return ResponseEntity.ok(orderFacade.placeOrderWithPayment(requestDto));
    }
}
