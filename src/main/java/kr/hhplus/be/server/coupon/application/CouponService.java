package kr.hhplus.be.server.coupon.application;

import kr.hhplus.be.server.coupon.application.dto.UserCouponResponseDto;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import kr.hhplus.be.server.coupon.domain.UserCoupon;

public interface CouponService {
    CouponPolicy getCouponPolicyDomain(long couponPolicyId);
    UserCoupon getCouponDomain(long userCouponId);
    UserCouponResponseDto getCoupon(long userCouponId);
    UserCouponResponseDto useCoupon(long userCouponId);
}
