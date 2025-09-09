package kr.hhplus.be.server.order.infra.kafka;

import kr.hhplus.be.server.order.domain.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.order-placed}")
    private String orderPlacedTopic;

    public void sendOrderPlacedEvent(OrderPlacedEvent event) {
        CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(orderPlacedTopic, event.eventId(), event);
        
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("주문 정보 이벤트 전송 실패: orderId={}", event.orderId(), throwable);
            } else {
                log.info("주문 정보 이벤트 전송 성공: orderId={}, offset={}",
                        event.orderId(), result.getRecordMetadata().offset());
            }
        });
    }
}