package kr.hhplus.be.server.coupon.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.hhplus.be.server.coupon.domain.CouponStatus;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_coupon")
@Getter
@Setter
@NoArgsConstructor()
@AllArgsConstructor
public class UserCouponJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long couponPolicyId;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    public UserCoupon toDomain(){
        return UserCoupon.builder()
            .id(this.id)
            .couponPolicyId(this.couponPolicyId)
            .userId(this.userId)
            .issuedAt(this.issuedAt)
            .status(this.status)
            .build();
    }

    public static UserCouponJpaEntity fromDomain(UserCoupon userCoupon){
        return new UserCouponJpaEntity(
            userCoupon.getId(),
            userCoupon.getCouponPolicyId(),
            userCoupon.getUserId(),
            userCoupon.getIssuedAt(),
            userCoupon.getStatus()
        );
    }
}
