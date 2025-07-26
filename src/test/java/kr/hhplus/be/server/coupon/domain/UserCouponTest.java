package kr.hhplus.be.server.coupon.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import kr.hhplus.be.server.coupon.domain.exception.CouponDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UserCouponTest 테스트")
public class UserCouponTest {

    @Nested
    @DisplayName("쿠폰 사용 시")
    class 쿠폰_사용_시{

        @Test
        @DisplayName("정상적으로 성공한다")
        void 정상적으로_성공(){
            long couponPolicyId = 1L;
            long userId = 1L;
            UserCoupon issuedUserCoupon = UserCoupon.builder()
                .build()
                .create(couponPolicyId, userId);

            UserCoupon usedUserCoupon = issuedUserCoupon.useCoupon();

            assertAll(
                () -> assertThat(usedUserCoupon).isNotNull(),
                () -> assertThat(usedUserCoupon.getStatus()).isEqualByComparingTo(CouponStatus.USED)
            );
        }

        @Test
        @DisplayName("이미 사용된 쿠폰은 실패한다")
        void 이미_사용된_쿠폰은_실패(){
            UserCoupon issuedUserCoupon = UserCoupon.builder()
                .id(1L)
                .couponPolicyId(1L)
                .userId(1L)
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.USED)
                .build();

            assertThatThrownBy(issuedUserCoupon::useCoupon)
                .isInstanceOf(CouponDomainException.AlreadyUsedCouponException.class);
        }
    }

}
