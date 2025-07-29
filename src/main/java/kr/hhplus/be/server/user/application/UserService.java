package kr.hhplus.be.server.user.application;

import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.user.application.dto.UserResponseDto;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.domain.port.UserRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponseDto chargeBalance(long userId, BigDecimal amount) {
        User originalUser = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User updatedUser = originalUser.chargeBalance(amount);

        User persistedUser = userRepository.insertOrUpdate(updatedUser);

        return UserResponseDto.from(persistedUser);
    }
}
