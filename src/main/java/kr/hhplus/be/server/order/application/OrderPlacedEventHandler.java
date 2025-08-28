package kr.hhplus.be.server.order.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.order.application.dto.ExternalOrderMessage;
import kr.hhplus.be.server.order.domain.event.port.ExternalOrderSender;
import kr.hhplus.be.server.order.domain.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPlacedEventHandler {

    private final ExternalOrderSender externalOrderSender;
    private final ObjectMapper objectMapper;

    @Async("orderEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        try {
            ExternalOrderMessage message = ExternalOrderMessage.from(event);
            String payloadJson = objectMapper.writeValueAsString(message);

            externalOrderSender.send(event.eventId(), payloadJson);

        } catch (Exception e) {
            log.error("주문정보 외부 플랫폼 전송 실패: eventId={}, orderId={} errorMsg={}",
                event.eventId(), event.orderId(), e.getMessage()
            );
        }
    }
}
