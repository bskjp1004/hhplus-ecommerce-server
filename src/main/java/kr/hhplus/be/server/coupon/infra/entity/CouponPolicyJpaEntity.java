package kr.hhplus.be.server.coupon.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coupon_policy")
@Getter
@Setter
@NoArgsConstructor()
@AllArgsConstructor
public class CouponPolicyJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private BigDecimal discountRate;

    @Column(nullable = false)
    private Integer totalCount;

    @Column(nullable = false)
    private Integer remainingCount;

    public CouponPolicy toDomain(){
        return CouponPolicy.builder()
            .id(this.id)
            .discountRate(this.discountRate)
            .totalCount(this.totalCount)
            .remainingCount(this.remainingCount)
            .build();
    }

    public static CouponPolicyJpaEntity fromDomain(CouponPolicy couponPolicy){
        return new CouponPolicyJpaEntity(
            couponPolicy.getId(),
            couponPolicy.getDiscountRate(),
            couponPolicy.getTotalCount(),
            couponPolicy.getRemainingCount()
        );
    }
}
