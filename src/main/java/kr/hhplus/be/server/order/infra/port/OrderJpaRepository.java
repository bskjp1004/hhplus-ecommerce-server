package kr.hhplus.be.server.order.infra.port;

import java.time.LocalDateTime;
import java.util.List;
import kr.hhplus.be.server.order.infra.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
    List<OrderJpaEntity> findAllByUserId(long userId);
    
    @Query("SELECT o.id FROM OrderJpaEntity o WHERE o.orderedAt > :dateTime")
    List<Long> findOrderIdsByOrderedAtAfter(@Param("dateTime") LocalDateTime dateTime);
}
