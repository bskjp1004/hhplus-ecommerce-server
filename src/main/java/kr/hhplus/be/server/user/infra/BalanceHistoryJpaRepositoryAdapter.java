package kr.hhplus.be.server.user.infra;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.user.domain.BalanceHistory;
import kr.hhplus.be.server.user.domain.port.BalanceHistoryRepository;
import kr.hhplus.be.server.user.infra.entity.BalanceHistoryJpaEntity;
import kr.hhplus.be.server.user.infra.port.BalanceHistoryJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class BalanceHistoryJpaRepositoryAdapter implements BalanceHistoryRepository {

    private final BalanceHistoryJpaRepository jpaRepository;

    @Override
    public Optional<BalanceHistory> findById(long id) {
        return Optional.of(jpaRepository.findById(id).get().toDomain());
    }

    @Override
    public List<BalanceHistory> findAllByUserId(long userId) {
        return jpaRepository.findAllByUserId(userId)
            .stream()
            .map(BalanceHistoryJpaEntity::toDomain)
            .toList();
    }

    @Override
    public BalanceHistory insertOrUpdate(BalanceHistory balanceHistory) {
        BalanceHistoryJpaEntity entity = BalanceHistoryJpaEntity.fromDomain(balanceHistory);
        BalanceHistoryJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }
}
