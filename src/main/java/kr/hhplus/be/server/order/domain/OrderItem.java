package kr.hhplus.be.server.order.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItem {
    private final long id;
    private final long orderId;
    private final long productId;
    private final Integer quantity;
    private final BigDecimal productPrice;

    public BigDecimal calculatePrice() {
        return productPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
