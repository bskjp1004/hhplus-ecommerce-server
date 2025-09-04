package kr.hhplus.be.server.coupon.domain.port;

import java.util.List;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import java.util.Optional;

public interface UserCouponRepository {
    Optional<UserCoupon> findById(long id);
    UserCoupon insertOrUpdate(UserCoupon userCoupon);
    List<UserCoupon> findAllByCouponPolicyId(long couponPolicyId);
    Optional<UserCoupon> findByUserIdAndCouponPolicyId(long userId, long couponPolicyId);
}
