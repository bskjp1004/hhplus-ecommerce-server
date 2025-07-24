package kr.hhplus.be.server.user.application.dto;

import kr.hhplus.be.server.user.domain.User;
import java.math.BigDecimal;

public record UserResponseDto(
    long id,
    BigDecimal balance
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getBalance()
        );
    }
}
