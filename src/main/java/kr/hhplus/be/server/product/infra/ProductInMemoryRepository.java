package kr.hhplus.be.server.product.infra;

import java.util.List;
import java.util.Objects;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.port.ProductRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import kr.hhplus.be.server.product.infra.entity.ProductEntity;
import org.springframework.stereotype.Repository;

@Repository
public class ProductInMemoryRepository implements ProductRepository {

    private final Map<Long, ProductEntity> storage = new HashMap<>();

    @Override
    public Optional<Product> findById(long productId) {
        return Optional.of(storage.get(productId).toDomain());
    }

    @Override
    public List<Product> findAllById(List<Long> productIds) {
        return productIds.stream()
            .map(storage::get)
            .filter(Objects::nonNull)
            .map(ProductEntity::toDomain)
            .toList();
    }

    @Override
    public Product insertOrUpdate(Product product) {
        ProductEntity productEntity = ProductEntity.fromDomain(product);
        storage.put(product.getId(), productEntity);
        return storage.get(product.getId()).toDomain();
    }
}
