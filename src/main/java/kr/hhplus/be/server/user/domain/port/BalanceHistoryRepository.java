package kr.hhplus.be.server.user.domain.port;

import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.user.domain.BalanceHistory;

public interface BalanceHistoryRepository {
    Optional<BalanceHistory> findById(long id);
    List<BalanceHistory> findAllByUserId(long userId);
    BalanceHistory insertOrUpdate(BalanceHistory balanceHistory);
}
