package kr.hhplus.be.server.user.application;

import kr.hhplus.be.server.user.application.dto.UserResponseDto;
import java.math.BigDecimal;

public interface UserService {
    UserResponseDto chargeBalance(long userId, BigDecimal amount);
}
