package kr.hhplus.be.server.product.domain.port;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.product.domain.Product;

public interface ProductRepository {
    Optional<Product> findById(long productId);
    List<Product> findAllById(List<Long> productIds);
    Product insertOrUpdate(Product product);
}
