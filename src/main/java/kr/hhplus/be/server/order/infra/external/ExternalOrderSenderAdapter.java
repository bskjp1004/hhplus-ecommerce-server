package kr.hhplus.be.server.order.infra.external;

import kr.hhplus.be.server.order.domain.event.port.ExternalOrderSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExternalOrderSenderAdapter implements ExternalOrderSender {

    @Override
    public void send(String idempotencyKey, String payloadJson) {
        log.info("주문정보 외부 플랫폼 전송: key={}, payload={}", idempotencyKey, payloadJson);
    }
}
