package kr.hhplus.be.server.coupon.infra.entity;

import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import kr.hhplus.be.server.coupon.domain.CouponStatus;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import lombok.AllArgsConstructor;

@Entity
@AllArgsConstructor
public class UserCouponEntity {
    private final long id;
    private final long couponPolicyId;
    private final long userId;
    private final LocalDateTime issuedAt;
    private final CouponStatus status;

    public UserCoupon toDomain(){
        return UserCoupon.builder()
            .id(this.id)
            .couponPolicyId(this.couponPolicyId)
            .userId(this.userId)
            .issuedAt(this.issuedAt)
            .status(this.status)
            .build();
    }

    public static UserCouponEntity fromDomain(UserCoupon userCoupon){
        return new UserCouponEntity(
            userCoupon.getId(),
            userCoupon.getCouponPolicyId(),
            userCoupon.getUserId(),
            userCoupon.getIssuedAt(),
            userCoupon.getStatus()
        );
    }
}
