package kr.hhplus.be.server.coupon.domain.port;

import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import java.util.Optional;

public interface CouponPolicyRepository {
    Optional<CouponPolicy> findById(long couponPolicyId);
    CouponPolicy insertOrUpdate(CouponPolicy couponPolicy);
}
