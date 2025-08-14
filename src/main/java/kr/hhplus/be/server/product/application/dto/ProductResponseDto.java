package kr.hhplus.be.server.product.application.dto;

import kr.hhplus.be.server.product.domain.Product;
import java.math.BigDecimal;

public record ProductResponseDto(
    Long id,
    BigDecimal price,
    Integer stock
) {
    public static ProductResponseDto from(Product product) {
        return new ProductResponseDto(
            product.getId(),
            product.getPrice(),
            product.getStock()
        );
    }
}
