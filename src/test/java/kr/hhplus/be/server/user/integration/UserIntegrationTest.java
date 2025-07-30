package kr.hhplus.be.server.user.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.user.application.UserService;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.domain.port.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("유저 통합 테스트")
public class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("성공 시나리오 테스트")
    class 성공_시나리오_테스트{

        @Test
        @DisplayName("잔액을 충전할 수 있다")
        void 잔액을_충전할_수_있다() {
            // given
            BigDecimal 충전잔액 = BigDecimal.valueOf(10_000L);
            User user = userRepository.insertOrUpdate(User.empty());

            // when
            var result = userService.chargeBalance(user.getId(), 충전잔액);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.balance()).isEqualByComparingTo(충전잔액)
            );
        }
    }

    @Nested
    @DisplayName("실패 시나리오 테스트")
    class 실패_시나리오_테스트{

        @Test
        @DisplayName("존재하지 않는 유저의 잔액은 충전할 수 없다")
        void 존재하지_않는_유저의_잔액은_충전할_수_없다() {
            // when & then
            assertThatThrownBy(() -> userService.chargeBalance(1L, BigDecimal.valueOf(10_000)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }
}
