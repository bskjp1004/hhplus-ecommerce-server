package kr.hhplus.be.server.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import kr.hhplus.be.server.user.domain.exception.UserDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("User 테스트")
public class UserTest {

    @Nested
    @DisplayName("잔액 충전 시")
    class 잔액_충전_시{

        @ParameterizedTest
        @DisplayName("요청 금액이 양수면 성공한다")
        @CsvSource({
            "1000",
            "2500.50",
            "9999999"
        })
        void 요청_금액이_양수면_성공(BigDecimal amount){
            User originalUser = User.empty();

            User updatedUser = originalUser.chargeBalance(amount);

            assertAll(
                ()->assertThat(updatedUser.getBalance()).isEqualTo(amount)
            );
        }

        @ParameterizedTest
        @DisplayName("요청 금액이 음수나 0이면 실패한다")
        @CsvSource({
            "-1",
            "0"
        })
        void 요청_금액이_음수나_0이면_실패(BigDecimal amount){
            User user = User.empty();

            assertThatThrownBy(() -> user.chargeBalance(amount))
                .isInstanceOf(UserDomainException.IllegalAmountException.class);
        }

        @Test
        @DisplayName("요청 금액이 최대 잔고를 넘어가면 실패한다")
        void 요청_금액이_최대잔고_넘으면_실패(){
            BigDecimal amount = BigDecimal.valueOf(10_000_001);
            User user = User.empty();

            assertThatThrownBy(() -> user.chargeBalance(amount))
                .isInstanceOf(UserDomainException.ExceedMaxBalanceException.class);
        }
    }
}
