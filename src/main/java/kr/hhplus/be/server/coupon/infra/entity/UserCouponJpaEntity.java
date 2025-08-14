package kr.hhplus.be.server.coupon.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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

    @Version
    private Long version = 0L;

    public UserCoupon toDomain(){
        return UserCoupon.builder()
            .id(this.id)
            .couponPolicyId(this.couponPolicyId)
            .userId(this.userId)
            .issuedAt(this.issuedAt)
            .status(this.status)
            .version(this.version)
            .build();
    }

    public static UserCouponJpaEntity fromDomain(UserCoupon userCoupon){
        UserCouponJpaEntity entity = new UserCouponJpaEntity();
        entity.setId(userCoupon.getId());
        entity.setCouponPolicyId(userCoupon.getCouponPolicyId());
        entity.setUserId(userCoupon.getUserId());
        entity.setIssuedAt(userCoupon.getIssuedAt());
        entity.setStatus(userCoupon.getStatus());
        if (userCoupon.getVersion() != null) {
            entity.setVersion(userCoupon.getVersion());
        }
        return entity;
    }
}
