package kr.hhplus.be.server.coupon.infra.port;

import java.util.List;
import kr.hhplus.be.server.coupon.infra.entity.UserCouponJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponJpaRepository extends JpaRepository<UserCouponJpaEntity, Long> {

    List<UserCouponJpaEntity> findAllByCouponPolicyId(long couponPolicyId);
}
