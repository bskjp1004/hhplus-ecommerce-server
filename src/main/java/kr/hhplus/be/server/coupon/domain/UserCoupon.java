package kr.hhplus.be.server.coupon.domain;

import kr.hhplus.be.server.coupon.domain.exception.CouponDomainException;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserCoupon {
    private final long id;
    private final long couponPolicyId;
    private final long userId;
    private final LocalDateTime issuedAt;
    private final CouponStatus status;

    @Builder
    private UserCoupon(long id, long couponPolicyId, long userId,
        LocalDateTime issuedAt, CouponStatus status
    ){
        this.id = id;
        this.couponPolicyId = couponPolicyId;
        this.userId = userId;
        this.issuedAt = issuedAt;
        this.status = status;
    }

    public UserCoupon create(long couponPolicyId, long userId){
        LocalDateTime nowDateTime = LocalDateTime.now();
        return UserCoupon.builder()
            .couponPolicyId(couponPolicyId)
            .userId(userId)
            .issuedAt(nowDateTime)
            .status(CouponStatus.ISSUED)
            .build();
    }

    public boolean canUse(){
        return this.status == CouponStatus.ISSUED;
    }

    public UserCoupon useCoupon(){
        if (!this.canUse()) {
            throw new CouponDomainException.AlreadyUsedCouponException();
        }
        return UserCoupon.builder()
            .status(CouponStatus.USED)
            .build();
    }
}
