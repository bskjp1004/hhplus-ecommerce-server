package kr.hhplus.be.server.user.infra.port;

import java.util.List;
import kr.hhplus.be.server.user.infra.entity.BalanceHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceHistoryJpaRepository extends JpaRepository<BalanceHistoryJpaEntity, Long> {
    List<BalanceHistoryJpaEntity> findAllByUserId(long userId);
}
