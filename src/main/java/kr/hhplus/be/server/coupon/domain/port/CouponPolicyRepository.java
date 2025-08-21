package kr.hhplus.be.server.coupon.domain.port;

import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import java.util.Optional;

public interface CouponPolicyRepository {
    Optional<CouponPolicy> findById(long id);
    Optional<CouponPolicy> findByIdWithLock(long id);
    CouponPolicy insertOrUpdate(CouponPolicy couponPolicy);
}
