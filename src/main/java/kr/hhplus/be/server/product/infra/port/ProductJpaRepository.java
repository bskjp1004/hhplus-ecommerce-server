package kr.hhplus.be.server.product.infra.port;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.hhplus.be.server.product.infra.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ProductJpaEntity c where c.id = :id")
    Optional<ProductJpaEntity> findByIdWithLock(@Param("id") long id);
}
