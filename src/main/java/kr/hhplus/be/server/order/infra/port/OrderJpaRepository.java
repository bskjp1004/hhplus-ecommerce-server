package kr.hhplus.be.server.order.infra.port;

import java.util.List;
import kr.hhplus.be.server.order.infra.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
    List<OrderJpaEntity> findAllByUserId(long userId);
}
