package kr.hhplus.be.server.product.infra;

import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProductInMemoryRepository implements ProductRepository {

    private final Map<Long, ProductEntity> storage = new HashMap<>();

    @Override
    public Optional<Product> findById(long productId) {
        return Optional.of(storage.get(productId).toDomain());
    }

    @Override
    public Product insertOrUpdate(Product product) {
        ProductEntity productEntity = ProductEntity.fromDomain(product);
        storage.put(product.getId(), productEntity);
        return storage.get(product.getId()).toDomain();
    }
}
