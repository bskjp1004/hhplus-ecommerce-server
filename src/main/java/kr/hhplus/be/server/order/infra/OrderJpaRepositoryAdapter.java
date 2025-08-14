package kr.hhplus.be.server.order.infra;

import java.time.LocalDateTime;
import java.util.List;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.port.OrderRepository;
import kr.hhplus.be.server.order.infra.entity.OrderJpaEntity;
import kr.hhplus.be.server.order.infra.port.OrderJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class OrderJpaRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    public Optional<Order> findById(long orderId) {
        return jpaRepository.findById(orderId)
            .map(OrderJpaEntity::toDomain);
    }

    @Override
    public List<Order> findAllByUserId(long userId) {
        return jpaRepository.findAllByUserId(userId)
            .stream()
            .map(OrderJpaEntity::toDomain)
            .toList();
    }

    @Override
    public Order insertOrUpdate(Order order) {
        OrderJpaEntity entity = OrderJpaEntity.fromDomain(order);
        OrderJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }
    
    @Override
    public List<Long> findOrderIdsByOrderedAtAfter(LocalDateTime dateTime) {
        return jpaRepository.findOrderIdsByOrderedAtAfter(dateTime);
    }
}