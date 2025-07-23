package kr.hhplus.be.server.product.infra;

import kr.hhplus.be.server.product.domain.Product;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductEntity {
    private long id;
    private BigDecimal price;
    private long stock;

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
