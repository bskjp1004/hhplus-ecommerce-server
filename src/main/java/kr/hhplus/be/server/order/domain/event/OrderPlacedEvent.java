package kr.hhplus.be.server.order.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(
    String eventId,
    Long orderId,
    Long userId,
    BigDecimal paidPrice,
    List<Item> items,
    Instant occurredAt
) {
    public static OrderPlacedEvent of(Long orderId, Long userId, BigDecimal paidPrice, List<Item> items) {
        return new OrderPlacedEvent(
            UUID.randomUUID().toString(),
            orderId,
            userId,
            paidPrice,
            items,
            Instant.now()
        );
    }
    public record Item(
        Long productId,
        int quantity,
        BigDecimal unitPrice
    ) {}
}
