package kr.hhplus.be.server.product.infra.entity;

import kr.hhplus.be.server.product.domain.Product;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;

@Entity
@AllArgsConstructor
public class ProductEntity {
    private long id;
    private BigDecimal price;
    private Integer stock;

    public Product toDomain() {
        return Product.builder()
            .id(this.id)
            .price(this.price)
            .stock(this.stock)
            .build();
    }

    public static ProductEntity fromDomain(Product product) {
        return new ProductEntity(
            product.getId(),
            product.getPrice(),
            product.getStock()
        );
    }
}
