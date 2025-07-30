package kr.hhplus.be.server.coupon.infra.port;

import kr.hhplus.be.server.coupon.infra.entity.CouponPolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponPolicyJpaRepository extends JpaRepository<CouponPolicyJpaEntity, Long> {

}
