package kr.hhplus.be.server.coupon.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssueRequest {
    
    private String messageId;
    private Long userId;
    private Long couponPolicyId;
    private LocalDateTime requestedAt;
    private String clientIp;
    private int retryCount;
    
    public static CouponIssueRequest create(Long userId, Long couponPolicyId) {
        return CouponIssueRequest.builder()
            .messageId(java.util.UUID.randomUUID().toString())
            .userId(userId)
            .couponPolicyId(couponPolicyId)
            .requestedAt(LocalDateTime.now())
            .retryCount(0)
            .build();
    }
    
    public CouponIssueRequest withRetry() {
        return CouponIssueRequest.builder()
            .messageId(this.messageId)
            .userId(this.userId)
            .couponPolicyId(this.couponPolicyId)
            .requestedAt(this.requestedAt)
            .clientIp(this.clientIp)
            .retryCount(this.retryCount + 1)
            .build();
    }
}