package kr.hhplus.be.server.order.domain.port;

import java.util.Optional;
import kr.hhplus.be.server.order.domain.Order;

public interface OrderRepository {
    Optional<Order> findById(long orderId);
    Order insertOrUpdate(Order order);
}
