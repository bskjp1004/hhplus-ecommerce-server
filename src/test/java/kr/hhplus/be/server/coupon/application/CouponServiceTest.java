package kr.hhplus.be.server.coupon.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.coupon.application.dto.UserCouponResponseDto;
import kr.hhplus.be.server.coupon.domain.CouponStatus;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import kr.hhplus.be.server.coupon.domain.exception.CouponDomainException;
import kr.hhplus.be.server.coupon.domain.port.UserCouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponServiceTest 테스트")
public class CouponServiceTest {

    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private CouponServiceAdapter couponService;

    @Nested
    @DisplayName("발급된 쿠폰 사용 시")
    class 발급된_쿠폰_사용_시{

        @Test
        @DisplayName("사용 가능한 쿠폰이면 성공한다")
        void 사용_가능한_쿠폰이면_성공(){
            long userCouponId = 1L;
            UserCoupon mockissuedUserCoupon = UserCoupon.builder()
                .id(userCouponId)
                .couponPolicyId(1L)
                .userId(1L)
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.ISSUED)
                .build();
            Mockito.when(userCouponRepository.findById(userCouponId))
                .thenReturn(Optional.of(mockissuedUserCoupon));
            Mockito.when(userCouponRepository.insertOrUpdate(Mockito.any(UserCoupon.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            UserCouponResponseDto userCouponResponseDto = couponService.useCoupon(userCouponId);

            assertAll(
                ()->assertThat(userCouponResponseDto).isNotNull(),
                ()->assertThat(userCouponResponseDto.status()).isEqualTo(CouponStatus.USED)
            );
        }

        @Test
        @DisplayName("이미 사용된 쿠폰이면 실패한다")
        void 이미_사용된_쿠폰이면_실패(){
            long userCouponId = 1L;
            UserCoupon mockissuedUserCoupon = UserCoupon.builder()
                .id(userCouponId)
                .couponPolicyId(1L)
                .userId(1L)
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.USED)
                .build();

            Mockito.when(userCouponRepository.findById(userCouponId))
                .thenReturn(Optional.of(mockissuedUserCoupon));

            assertThatThrownBy(() -> couponService.useCoupon(userCouponId))
                .isInstanceOf(CouponDomainException.AlreadyUsedCouponException.class);
        }
    }
}
