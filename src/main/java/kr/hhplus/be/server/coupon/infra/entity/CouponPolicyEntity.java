package kr.hhplus.be.server.coupon.infra.entity;

import jakarta.persistence.Entity;
import java.math.BigDecimal;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import lombok.AllArgsConstructor;

@Entity
@AllArgsConstructor
public class CouponPolicyEntity {
    private final long id;
    private final BigDecimal discountRate;
    private final Integer totalCount;
    private final Integer remainingCount;

    public CouponPolicy toDomain(){
        return CouponPolicy.builder()
            .id(this.id)
            .discountRate(this.discountRate)
            .totalCount(this.totalCount)
            .remainingCount(this.remainingCount)
            .build();
    }

    public static CouponPolicyEntity fromDomain(CouponPolicy couponPolicy){
        return new CouponPolicyEntity(
            couponPolicy.getId(),
            couponPolicy.getDiscountRate(),
            couponPolicy.getTotalCount(),
            couponPolicy.getRemainingCount()
        );
    }
}
