package kr.hhplus.be.server.product.controller.dto;

import java.math.BigDecimal;
import kr.hhplus.be.server.product.application.dto.ProductResult;

public record ProductResponse(
    Long id,
    BigDecimal price,
    Integer stock
) {

    public static ProductResponse from(ProductResult result) {
        return new ProductResponse(
            result.id(),
            result.price(),
            result.stock()
        );
    }
}
