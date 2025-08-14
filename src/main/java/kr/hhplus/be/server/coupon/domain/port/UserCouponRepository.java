package kr.hhplus.be.server.coupon.domain.port;

import kr.hhplus.be.server.coupon.domain.UserCoupon;
import java.util.Optional;

public interface UserCouponRepository {
    Optional<UserCoupon> findById(long userCouponId);
    UserCoupon insertOrUpdate(UserCoupon userCoupon);
}
