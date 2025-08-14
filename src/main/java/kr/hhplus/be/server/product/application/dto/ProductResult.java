package kr.hhplus.be.server.product.application.dto;

import kr.hhplus.be.server.product.domain.Product;
import java.math.BigDecimal;

public record ProductResult(
    Long id,
    BigDecimal price,
    Integer stock
) {
    public static ProductResult from(Product product) {
        return new ProductResult(
            product.getId(),
            product.getPrice(),
            product.getStock()
        );
    }
}
