package kr.hhplus.be.server.order.infra.port;

import kr.hhplus.be.server.order.infra.entity.OrderItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemJpaEntity, Long> {

}
