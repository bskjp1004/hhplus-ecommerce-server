package kr.hhplus.be.server.product.controller.dto;

import java.math.BigDecimal;
import kr.hhplus.be.server.product.domain.Product;

public record  ProductResponse(
    Long id,
    BigDecimal price,
    Long stock
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getPrice(), product.getStock());
    }
}
