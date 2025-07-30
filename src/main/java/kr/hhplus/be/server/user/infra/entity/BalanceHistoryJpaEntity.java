package kr.hhplus.be.server.user.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import kr.hhplus.be.server.user.domain.BalanceHistory;
import kr.hhplus.be.server.user.domain.BalanceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "balance_history")
@Getter
@Setter
@NoArgsConstructor()
@AllArgsConstructor
public class BalanceHistoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BalanceStatus balanceType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime processedAt;

    public BalanceHistory toDomain(){
        return BalanceHistory.builder()
            .id(this.id)
            .userId(this.userId)
            .balanceType(this.balanceType)
            .amount(this.amount)
            .processedAt(this.processedAt)
            .build();
    }

    public static BalanceHistoryJpaEntity fromDomain(BalanceHistory balanceHistory) {
        return new BalanceHistoryJpaEntity(
            balanceHistory.getId(),
            balanceHistory.getUserId(),
            balanceHistory.getBalanceType(),
            balanceHistory.getAmount(),
            balanceHistory.getProcessedAt()
        );
    }
}
