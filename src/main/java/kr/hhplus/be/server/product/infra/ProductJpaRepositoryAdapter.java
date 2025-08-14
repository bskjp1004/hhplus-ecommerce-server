package kr.hhplus.be.server.product.infra;

import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.port.ProductRepository;
import kr.hhplus.be.server.product.infra.entity.ProductJpaEntity;
import kr.hhplus.be.server.product.infra.port.ProductJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class ProductJpaRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    @Override
    public Optional<Product> findById(long productId) {
        return jpaRepository.findOneById(productId)
            .map(ProductJpaEntity::toDomain);
    }

    @Override
    public List<Product> findAllById(List<Long> productIds) {
        return jpaRepository.findAllById(productIds).stream()
            .map(ProductJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Product insertOrUpdate(Product product) {
        ProductJpaEntity entity = ProductJpaEntity.fromDomain(product);
        ProductJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }
}