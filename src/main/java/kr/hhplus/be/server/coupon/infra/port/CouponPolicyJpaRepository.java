package kr.hhplus.be.server.coupon.infra.port;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.coupon.infra.entity.CouponPolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponPolicyJpaRepository extends JpaRepository<CouponPolicyJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CouponPolicyJpaEntity> findOneById(Long id);
}
