package kr.hhplus.be.server.user.infra.entity;

import kr.hhplus.be.server.user.domain.User;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;

@Entity
@AllArgsConstructor
public class UserEntity {
    private long id;
    private BigDecimal balance;

    public User toDomain(){
        return User.builder()
            .id(this.id)
            .balance(this.balance)
            .build();
    }

    public static UserEntity fromDomain(User user){
        return new UserEntity(
            user.getId(),
            user.getBalance()
        );
    }
}
