package kr.hhplus.be.server.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;

import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.user.application.dto.UserResponseDto;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.domain.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import kr.hhplus.be.server.user.domain.exception.UserDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceTest 테스트")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceAdapter userService;

    @Nested
    @DisplayName("잔액 충전 시")
    class 잔액_충전_시{

        @Test
        @DisplayName("정상적인 요청이면 충전에 성공한다")
        void 정상적인_요청이면_충전에_성공(){
            long userId = 1L;
            BigDecimal originalBalance = BigDecimal.ZERO;
            BigDecimal requestAmount = BigDecimal.valueOf(1_000L);
            BigDecimal expectedBalance = originalBalance.add(requestAmount);

            User originalUser = User.builder()
                .id(userId)
                .balance(originalBalance)
                .build();

            Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(originalUser));

            Mockito.when(userRepository.insertOrUpdate(Mockito.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            UserResponseDto updatedUserDto = userService.chargeBalance(userId, requestAmount);

            assertAll(
                () -> assertThat(updatedUserDto).isNotNull(),
                () -> assertThat(updatedUserDto.balance()).isEqualTo(expectedBalance)
            );

        }

        @Test
        @DisplayName("존재하지 않는 유저면 실패한다")
        void 존재하지_않는_유저면_실패(){
            long userId = 1L;
            BigDecimal requestAmount = BigDecimal.valueOf(1_000L);
            Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.chargeBalance(userId, requestAmount);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @ParameterizedTest
        @DisplayName("요청 금액이 유효하지 않으면 실패한다")
        @CsvSource({
            "-1",
            "0"
        })
        void 요청_금액이_유효하지않으면_실패(BigDecimal requestAmount){
            long userId = 1L;
            BigDecimal originalBalance = BigDecimal.ZERO;

            User originalUser = User.builder()
                .id(userId)
                .balance(originalBalance)
                .build();

            Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(originalUser));

            assertThatThrownBy(() -> userService.chargeBalance(userId, requestAmount))
                .isInstanceOf(UserDomainException.IllegalAmountException.class);
        }

        @Test
        @DisplayName("최대 잔고를 초과하면 실패한다")
        void 최대_잔고를_초과하면_실패(){
            long userId = 1L;
            BigDecimal originalBalance = BigDecimal.valueOf(9_999_999L);
            BigDecimal requestAmount = BigDecimal.valueOf(1_000L);

            User originalUser = User.builder()
                .id(userId)
                .balance(originalBalance)
                .build();

            Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(originalUser));

            assertThatThrownBy(() -> userService.chargeBalance(userId, requestAmount))
                .isInstanceOf(UserDomainException.ExceedMaxBalanceException.class);
        }
    }
}
