package kr.hhplus.be.server.coupon.controller;

import kr.hhplus.be.server.coupon.application.CouponService;
import kr.hhplus.be.server.coupon.application.dto.CouponQueueResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("{userId}")
    public ResponseEntity<CouponQueueResponseDto> issueLimitedCoupon (
        @PathVariable long userId,
        @RequestBody long couponPolicyId
    ) {
        return ResponseEntity.ok(couponService.issueLimitedCouponFromRedis(userId, couponPolicyId));
    }
}
