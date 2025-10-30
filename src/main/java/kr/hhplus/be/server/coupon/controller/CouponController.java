package kr.hhplus.be.server.coupon.controller;

import kr.hhplus.be.server.coupon.application.CouponService;
import kr.hhplus.be.server.coupon.application.dto.CouponQueueResponseDto;
import kr.hhplus.be.server.coupon.application.dto.UserCouponResponseDto;
import kr.hhplus.be.server.coupon.infra.kafka.CouponKafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;
    private final CouponKafkaProducer couponKafkaProducer;

    @PostMapping("{couponPolicyId}")
    public ResponseEntity<CouponQueueResponseDto> issueLimitedCoupon (
        @PathVariable long couponPolicyId,
        @RequestBody long userId
    ) {
        CouponQueueResponseDto response = couponKafkaProducer.publishCouponIssueRequest(userId, couponPolicyId);
        return ResponseEntity.accepted().body(response);
    }
    
    @GetMapping("/status")
    public ResponseEntity<CouponQueueResponseDto> getCouponStatus(
        @RequestParam long userId,
        @RequestParam long couponPolicyId
    ) {
        try {
            // 발급된 쿠폰이 있는지 확인
            UserCouponResponseDto userCoupon = couponService.getUserCoupon(userId, couponPolicyId);
            
            if (userCoupon != null) {
                return ResponseEntity.ok(CouponQueueResponseDto.issued(userId, couponPolicyId));
            } else {
                // 쿠폰 정책 재고 확인
                boolean hasStock = couponService.hasRemainingStock(couponPolicyId);
                
                if (hasStock) {
                    return ResponseEntity.ok(CouponQueueResponseDto.pending(userId, couponPolicyId, null));
                } else {
                    return ResponseEntity.ok(CouponQueueResponseDto.soldOut(userId, couponPolicyId, "쿠폰 매진"));
                }
            }
        } catch (Exception e) {
            return ResponseEntity.ok(CouponQueueResponseDto.failed(userId, couponPolicyId, "상태 조회 실패"));
        }
    }
}
