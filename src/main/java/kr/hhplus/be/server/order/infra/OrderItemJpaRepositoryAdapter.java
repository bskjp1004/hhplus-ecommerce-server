package kr.hhplus.be.server.order.infra;

import kr.hhplus.be.server.order.domain.OrderItem;
import kr.hhplus.be.server.order.domain.port.OrderItemRepository;
import kr.hhplus.be.server.order.infra.entity.OrderItemJpaEntity;
import kr.hhplus.be.server.order.infra.port.OrderItemJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class OrderItemJpaRepositoryAdapter implements OrderItemRepository {

    private final OrderItemJpaRepository jpaRepository;

    @Override
    public Optional<OrderItem> findById(long orderItemId) {
        return jpaRepository.findById(orderItemId)
            .map(OrderItemJpaEntity::toDomain);
    }

    @Override
    public OrderItem insertOrUpdate(OrderItem orderItem) {
        OrderItemJpaEntity entity = OrderItemJpaEntity.fromDomain(orderItem);
        OrderItemJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }
}