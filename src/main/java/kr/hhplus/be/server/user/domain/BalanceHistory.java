package kr.hhplus.be.server.user.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BalanceHistory {
    private final Long id;
    private final Long userId;
    private final BalanceStatus balanceType;
    private final BigDecimal amount;
    private final LocalDateTime processedAt;

    public static BalanceHistory create(Long userId, BalanceStatus balanceType, BigDecimal amount) {
        LocalDateTime nowDateTime = LocalDateTime.now();
        return BalanceHistory.builder()
            .userId(userId)
            .balanceType(balanceType)
            .amount(amount)
            .processedAt(nowDateTime)
            .build();
    }
}
