package kr.hhplus.be.server.coupon.application;

import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.coupon.application.dto.UserCouponResponseDto;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import kr.hhplus.be.server.coupon.domain.port.CouponPolicyRepository;
import kr.hhplus.be.server.coupon.domain.port.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponServiceAdapter implements CouponService{

    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponRepository userCouponRepository;

    @Override
    public CouponPolicy getCouponPolicyDomain(long couponPolicyId) {
        return couponPolicyRepository.findById(couponPolicyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_POLICY_NOT_FOUND));
    }

    @Override
    public UserCoupon getCouponDomain(long userCouponId) {
        return userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
    }

    @Override
    public UserCouponResponseDto getCoupon(long userCouponId) {
        return UserCouponResponseDto.from(getCouponDomain(userCouponId));
    }

    @Override
    public UserCouponResponseDto useCoupon(long userCouponId) {
        UserCoupon issuedUserCoupon = userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        UserCoupon useCoupon = issuedUserCoupon.useCoupon();

        UserCoupon persistedUserCoupon = userCouponRepository.insertOrUpdate(useCoupon);

        return UserCouponResponseDto.from(persistedUserCoupon);
    }
}
