package kr.hhplus.be.server.product.infra.port;

import kr.hhplus.be.server.product.infra.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

}
