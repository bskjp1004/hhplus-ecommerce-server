package kr.hhplus.be.server.product.infra.port;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.hhplus.be.server.product.infra.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

}
