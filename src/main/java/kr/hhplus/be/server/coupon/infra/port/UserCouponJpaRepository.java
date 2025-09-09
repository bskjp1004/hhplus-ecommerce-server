package kr.hhplus.be.server.coupon.infra.port;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.coupon.infra.entity.UserCouponJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponJpaRepository extends JpaRepository<UserCouponJpaEntity, Long> {

    List<UserCouponJpaEntity> findAllByCouponPolicyId(long couponPolicyId);
    Optional<UserCouponJpaEntity> findByUserIdAndCouponPolicyId(long userId, long couponPolicyId);
}
