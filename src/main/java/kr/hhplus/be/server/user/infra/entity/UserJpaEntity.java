package kr.hhplus.be.server.user.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import kr.hhplus.be.server.user.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor()
@AllArgsConstructor
public class UserJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private BigDecimal balance;

    @Version
    private Long version = 0L;

    public User toDomain(){
        return User.builder()
            .id(this.id)
            .balance(this.balance)
            .version(this.version)
            .build();
    }

    public static UserJpaEntity fromDomain(User user){
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setBalance(user.getBalance());
        if (user.getVersion() != null) {
            entity.setVersion(user.getVersion());
        }
        return entity;
    }
}
