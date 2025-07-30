package kr.hhplus.be.server.order.controller;

import kr.hhplus.be.server.order.application.OrderFacade;
import kr.hhplus.be.server.order.application.dto.CreateOrderCommand;
import kr.hhplus.be.server.order.application.dto.OrderItemCommand;
import kr.hhplus.be.server.order.application.dto.OrderResult;
import kr.hhplus.be.server.order.controller.dto.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<OrderResult> getProduct(
        @RequestBody OrderCreateRequest requestDto
    ) {
        CreateOrderCommand command = new CreateOrderCommand(
            requestDto.userId(),
            requestDto.couponId(),
            requestDto.orderItems().stream()
                .map(item -> new OrderItemCommand(item.productId(), item.quantity()))
                .toList()
        );
        return ResponseEntity.ok(orderFacade.placeOrderWithPayment(command));
    }
}
