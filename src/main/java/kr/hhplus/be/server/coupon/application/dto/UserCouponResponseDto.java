package kr.hhplus.be.server.coupon.application.dto;

import kr.hhplus.be.server.coupon.domain.CouponStatus;
import kr.hhplus.be.server.coupon.domain.UserCoupon;

public record UserCouponResponseDto(
    long id,
    long userId,
    CouponStatus status
) {
    public static UserCouponResponseDto from(UserCoupon userCoupon){
        return new UserCouponResponseDto(
            userCoupon.getId(),
            userCoupon.getUserId(),
            userCoupon.getStatus()
        );
    }
}
