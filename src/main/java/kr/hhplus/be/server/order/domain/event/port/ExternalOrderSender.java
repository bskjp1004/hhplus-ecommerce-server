package kr.hhplus.be.server.order.domain.event.port;

public interface  ExternalOrderSender {
    void send(String idempotencyKey, String payloadJson);
}
