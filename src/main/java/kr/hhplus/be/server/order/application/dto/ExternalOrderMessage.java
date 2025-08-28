package kr.hhplus.be.server.order.application.dto;

import java.math.BigDecimal;
import java.util.List;
import kr.hhplus.be.server.order.domain.event.OrderPlacedEvent;

public record ExternalOrderMessage(
    Long orderId,
    Long userId,
    BigDecimal paidPrice,
    List<LineItem> items
) {
    public static ExternalOrderMessage from(OrderPlacedEvent e) {
        return new ExternalOrderMessage(
            e.orderId(),
            e.userId(),
            e.paidPrice(),
            e.items().stream()
                .map(i -> new LineItem(
                    i.productId(), i.quantity(), i.unitPrice()
                ))
                .toList()
        );
    }

    public record LineItem(
        Long productId,
        int quantity,
        BigDecimal unitPrice
    ) {}
}
