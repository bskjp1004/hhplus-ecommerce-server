package kr.hhplus.be.server.order.domain.port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.hhplus.be.server.order.domain.Order;

public interface OrderRepository {
    Optional<Order> findById(long orderId);
    List<Order> findAllByUserId(long userId);
    Order insertOrUpdate(Order order);
    List<Long> findOrderIdsByOrderedAtAfter(LocalDateTime dateTime);
}
