package kr.hhplus.be.server.coupon.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CouponPolicy {
    private final long id;
    private final BigDecimal discountRate;
    private final Integer totalCount;
    private final Integer remainingCount;

    @Builder
    private CouponPolicy(long id, BigDecimal discountRate, Integer totalCount, Integer remainingCount){
        this.id = id;
        this.discountRate = discountRate;
        this.totalCount = totalCount;
        this.remainingCount = remainingCount;
    }

    public static CouponPolicy create(BigDecimal discountRate, Integer totalCount){
        return CouponPolicy.builder()
            .discountRate(discountRate)
            .totalCount(totalCount)
            .remainingCount(0)
            .build();
    }

    public boolean canIssue() {
        return remainingCount > 0;
    }
}
