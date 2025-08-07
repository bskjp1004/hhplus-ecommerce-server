package kr.hhplus.be.server.coupon.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.coupon.application.CouponService;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import kr.hhplus.be.server.coupon.domain.port.CouponPolicyRepository;
import kr.hhplus.be.server.coupon.domain.port.UserCouponRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.domain.port.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("쿠폰 통합 테스트")
public class CouponIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponPolicyRepository couponPolicyRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Nested
    @DisplayName("성공 시나리오 테스트")
    class 성공_시나리오_테스트 {

        @Test
        @DisplayName("쿠폰을 발급할 수 있다")
        void 쿠폰을_발급할_수_있다() {
            // given
            // 1. 사용자 생성
            User user = userRepository.insertOrUpdate(
                User.empty()
            );
            // 2. 쿠폰 정책 생성
            CouponPolicy couponPolicy = couponPolicyRepository.insertOrUpdate(
                CouponPolicy.builder()
                    .discountRate(BigDecimal.valueOf(0.1))
                    .totalCount(100)
                    .remainingCount(50)
                    .build()
            );

            // when
            var result = couponService.issueLimitedCoupon(user.getId(), couponPolicy.getId());

            // then
            assertAll(
                () -> {
                    UserCoupon updatedUserCoupon = userCouponRepository.findById(result.id()).orElseThrow();
                    assertThat(updatedUserCoupon.getId()).isEqualTo(couponPolicy.getId());
                }
            );
        }
    }
}
