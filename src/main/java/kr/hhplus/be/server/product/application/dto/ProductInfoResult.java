package kr.hhplus.be.server.product.application.dto;

import java.math.BigDecimal;
import kr.hhplus.be.server.product.domain.Product;

public record ProductInfoResult (
    Long id,
    BigDecimal price
) {
    public static ProductInfoResult from(Product product) {
        return new ProductInfoResult(
            product.getId(),
            product.getPrice()
        );
    }
}
