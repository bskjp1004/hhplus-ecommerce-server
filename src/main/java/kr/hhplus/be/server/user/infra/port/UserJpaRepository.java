package kr.hhplus.be.server.user.infra.port;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.hhplus.be.server.user.infra.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserJpaEntity> findOneById(Long id);
}
