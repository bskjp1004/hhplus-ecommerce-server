package kr.hhplus.be.server.order.infra;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.hhplus.be.server.order.domain.OrderItem;
import kr.hhplus.be.server.order.domain.port.OrderItemRepository;
import kr.hhplus.be.server.order.infra.entity.OrderItemJpaEntity;
import kr.hhplus.be.server.order.infra.port.OrderItemJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    
    @Override
    public List<Map.Entry<Long, Long>> findTopSellingProductIds(List<Long> orderIds, int limit) {
        if (orderIds.isEmpty()) {
            return List.of();
        }
        
        List<Object[]> results = jpaRepository.findTopSellingProducts(orderIds, PageRequest.of(0, limit));
        
        return results.stream()
            .map(row -> new AbstractMap.SimpleEntry<>((Long) row[0], ((Number) row[1]).longValue()))
            .collect(Collectors.toList());
    }
}