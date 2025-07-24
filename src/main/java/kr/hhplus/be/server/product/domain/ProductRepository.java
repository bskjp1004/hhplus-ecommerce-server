package kr.hhplus.be.server.product.domain;

import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(long productId);
    Product insertOrUpdate(Product product);
}
