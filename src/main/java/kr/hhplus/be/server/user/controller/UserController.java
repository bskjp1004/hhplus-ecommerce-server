package kr.hhplus.be.server.user.controller;

import java.math.BigDecimal;
import kr.hhplus.be.server.user.application.UserService;
import kr.hhplus.be.server.user.application.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    
    @PatchMapping("{userId}/balance")
    public ResponseEntity<UserResponseDto> chargeBalance(
        @PathVariable long userId,
        @RequestBody BigDecimal balance
    ) {
        return ResponseEntity.ok(userService.chargeBalance(userId, balance));
    }
}
