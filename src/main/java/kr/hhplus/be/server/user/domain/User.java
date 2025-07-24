package kr.hhplus.be.server.user.domain;

import kr.hhplus.be.server.user.domain.exception.UserDomainException;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
    private static final BigDecimal MAX_BALANCE = BigDecimal.valueOf(10_000_000);
    private static final BigDecimal MIN_BALANCE = BigDecimal.ZERO;

    private final long id;
    private final BigDecimal balance;

    public static User empty(){
        return new User(0, BigDecimal.ZERO);
    }

    public User chargeBalance(BigDecimal amount) {
        if (amount.signum() <= 0){
            throw new UserDomainException.IllegalAmountException();
        }

        BigDecimal newBalance = this.balance.add(amount);

        if (newBalance.compareTo(MAX_BALANCE) > 0){
            throw new UserDomainException.ExceedMaxBalanceException();
        }
        return new User(this.id, newBalance);
    }

    public User useBalance(BigDecimal amount){
        if (amount.signum() <= 0){
            throw new UserDomainException.IllegalAmountException();
        }

        BigDecimal newBalance = this.balance.subtract(amount);

        if (newBalance.compareTo(MIN_BALANCE) < 0){
            throw new UserDomainException.InsufficientBalanceException();
        }
        return new User(this.id, newBalance);
    }
}
