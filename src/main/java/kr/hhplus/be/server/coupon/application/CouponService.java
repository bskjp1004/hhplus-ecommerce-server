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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponRepository userCouponRepository;

    public CouponPolicy getCouponPolicyDomain(long couponPolicyId) {
        return couponPolicyRepository.findById(couponPolicyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_POLICY_NOT_FOUND));
    }

    public UserCoupon getCouponDomain(long userCouponId) {
        return userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
    }

    public UserCouponResponseDto getCoupon(long userCouponId) {
        return UserCouponResponseDto.from(getCouponDomain(userCouponId));
    }

    @Transactional
    public UserCouponResponseDto useCoupon(long userCouponId) {
        UserCoupon issuedUserCoupon = userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        UserCoupon useCoupon = issuedUserCoupon.useCoupon();

        UserCoupon persistedUserCoupon = userCouponRepository.insertOrUpdate(useCoupon);

        return UserCouponResponseDto.from(persistedUserCoupon);
    }

    @Transactional
    public BigDecimal applyCouponForOrder(long couponId) {
        if (couponId <= 0) {
            return BigDecimal.ZERO;
        }
        
        UserCoupon userCoupon = getCouponDomain(couponId);
        useCoupon(userCoupon.getId());
        
        CouponPolicy couponPolicy = getCouponPolicyDomain(userCoupon.getCouponPolicyId());
        return couponPolicy.getDiscountRate();
    }
}
