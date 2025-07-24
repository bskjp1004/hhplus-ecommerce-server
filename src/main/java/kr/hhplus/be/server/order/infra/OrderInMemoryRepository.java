package kr.hhplus.be.server.order.infra;

import java.util.HashMap;
import java.util.Map;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.port.OrderRepository;
import java.util.Optional;
import kr.hhplus.be.server.order.infra.entity.OrderEntity;

public class OrderInMemoryRepository implements OrderRepository {

    private final Map<Long, OrderEntity> storage = new HashMap<>();

    @Override
    public Optional<Order> findById(long orderId) {
        return Optional.of(storage.get(orderId).toDomain());
    }

    @Override
    public Order insertOrUpdate(Order order) {
        OrderEntity orderEntity = OrderEntity.fromDomain(order);
        storage.put(order.getId(), orderEntity);
        return storage.get(order.getId()).toDomain();
    }
}
