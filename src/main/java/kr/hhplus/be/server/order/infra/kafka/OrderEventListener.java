package kr.hhplus.be.server.order.infra.kafka;

import kr.hhplus.be.server.order.application.dto.OrderResult;
import kr.hhplus.be.server.order.domain.event.OrderPlacedEvent;
import kr.hhplus.be.server.order.domain.event.OrderPlacedEvent.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderEventProducer orderEventProducer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPlacedEvent(OrderResult orderResult) {
        var items = orderResult.orderItems().stream()
            .map(oi -> new Item(
                oi.productId(),
                oi.quantity(),
                oi.unitPrice()
            ))
            .toList();

        OrderPlacedEvent event = OrderPlacedEvent.of(
            orderResult.id(),
            orderResult.userId(),
            orderResult.paidPrice(),
            items
        );

        try {
            orderEventProducer.sendOrderPlacedEvent(event);
        } catch (Exception e) {
            log.error("주문 정보 이벤트 전송 실패: orderId={}", event.orderId(), e);
        }
    }
}