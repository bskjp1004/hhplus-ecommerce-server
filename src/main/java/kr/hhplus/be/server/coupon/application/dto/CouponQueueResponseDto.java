package kr.hhplus.be.server.coupon.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponQueueResponseDto {
    
    private Long userId;
    private Long couponPolicyId;
    private Long rank;  // 선착순 순위
    private CouponQueueStatus status;
    private String message;
    
    public enum CouponQueueStatus {
        PENDING("발급 대기 중"),
        ISSUED("발급 완료"),
        FAILED("발급 실패"),
        SOLD_OUT("매진");
        
        private final String description;
        
        CouponQueueStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public static CouponQueueResponseDto pending(Long userId, Long couponPolicyId, Long rank) {
        return CouponQueueResponseDto.builder()
            .userId(userId)
            .couponPolicyId(couponPolicyId)
            .rank(rank)
            .status(CouponQueueStatus.PENDING)
            .message(String.format("선착순 %d번째로 등록되었습니다. 발급 처리 중입니다.", rank))
            .build();
    }
    
    public static CouponQueueResponseDto soldOut(Long userId, Long couponPolicyId, String reason) {
        return CouponQueueResponseDto.builder()
            .userId(userId)
            .couponPolicyId(couponPolicyId)
            .status(CouponQueueStatus.SOLD_OUT)
            .message(reason)
            .build();
    }
    
    public static CouponQueueResponseDto failed(Long userId, Long couponPolicyId, String reason) {
        return CouponQueueResponseDto.builder()
            .userId(userId)
            .couponPolicyId(couponPolicyId)
            .status(CouponQueueStatus.FAILED)
            .message(reason)
            .build();
    }

    public static CouponQueueResponseDto issued(Long userId, Long couponPolicyId) {
        return CouponQueueResponseDto.builder()
            .userId(userId)
            .couponPolicyId(couponPolicyId)
            .status(CouponQueueStatus.ISSUED)
            .message("쿠폰 발급이 완료되었습니다.")
            .build();
    }
}