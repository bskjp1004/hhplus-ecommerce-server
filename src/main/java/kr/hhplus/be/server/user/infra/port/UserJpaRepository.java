package kr.hhplus.be.server.user.infra.port;

import kr.hhplus.be.server.user.infra.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
}
