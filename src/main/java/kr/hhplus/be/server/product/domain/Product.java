package kr.hhplus.be.server.product.domain;

import kr.hhplus.be.server.product.domain.exception.ProductDomainException;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class  Product {
    private static final long MAX_STOCK = 10_000_000;
    private static final long MIN_STOCK = 0;

    private final long id;
    private final BigDecimal price;
    private final Integer stock;

    public Product decreaseStock(Integer stock){
        if (stock <= 0){
            throw new ProductDomainException.IllegalStockException();
        }

        if (stock > this.stock){
            throw new ProductDomainException.InsufficientStockException();
        }

        Integer newStock = this.stock - stock;

        return new Product(this.id, this.price, newStock);
    }
}
