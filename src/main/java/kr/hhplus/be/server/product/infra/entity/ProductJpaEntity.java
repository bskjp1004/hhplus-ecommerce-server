package kr.hhplus.be.server.product.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import kr.hhplus.be.server.product.domain.Product;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor()
@AllArgsConstructor
public class ProductJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    public Product toDomain() {
        return Product.builder()
            .id(this.id)
            .price(this.price)
            .stock(this.stock)
            .build();
    }

    public static ProductJpaEntity fromDomain(Product product) {
        return new ProductJpaEntity(
            product.getId(),
            product.getPrice(),
            product.getStock()
        );
    }
}
