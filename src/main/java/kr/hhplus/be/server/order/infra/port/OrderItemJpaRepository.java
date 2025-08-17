package kr.hhplus.be.server.order.infra.port;

import java.util.List;
import kr.hhplus.be.server.order.infra.entity.OrderItemJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemJpaEntity, Long> {
    
    @Query("SELECT oi.productId, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItemJpaEntity oi " +
           "WHERE oi.order.id IN :orderIds " +
           "GROUP BY oi.productId " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts(@Param("orderIds") List<Long> orderIds, Pageable pageable);
}
