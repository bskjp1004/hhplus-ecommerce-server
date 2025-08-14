package kr.hhplus.be.server.coupon.application;

import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.coupon.application.dto.UserCouponResponseDto;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import kr.hhplus.be.server.coupon.domain.exception.CouponDomainException;
import kr.hhplus.be.server.coupon.domain.port.CouponPolicyRepository;
import kr.hhplus.be.server.coupon.domain.port.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    public UserCouponResponseDto issueLimitedCoupon(long userId, long couponPolicyId){
        CouponPolicy couponPolicy = couponPolicyRepository.findById(couponPolicyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_POLICY_NOT_FOUND));

        if (!couponPolicy.canIssue())
        {
            throw new BusinessException(ErrorCode.COUPON_OUT_OF_STOCK);
        }

        couponPolicyRepository.insertOrUpdate(couponPolicy.issue());

        UserCoupon userCoupon = UserCoupon.create(couponPolicy.getId(), userId);

        UserCoupon persistedUserCoupon = userCouponRepository.insertOrUpdate(userCoupon);

        return UserCouponResponseDto.from(persistedUserCoupon);
    }

    @Transactional
    public UserCouponResponseDto useCoupon(long userCouponId) {
        UserCoupon issuedUserCoupon = userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        UserCoupon useCoupon = issuedUserCoupon.useCoupon();

        UserCoupon persistedUserCoupon = userCouponRepository.insertOrUpdate(useCoupon);

        return UserCouponResponseDto.from(persistedUserCoupon);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public BigDecimal applyCouponForOrder(long userCouponId) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        
        try {
            UserCoupon usedCoupon = userCoupon.useCoupon();
            userCouponRepository.insertOrUpdate(usedCoupon);
        } catch (OptimisticLockingFailureException e) {
            throw new CouponDomainException.AlreadyUsedCouponException();
        }
        
        CouponPolicy couponPolicy = getCouponPolicyDomain(userCoupon.getCouponPolicyId());
        return couponPolicy.getDiscountRate();
    }
}
