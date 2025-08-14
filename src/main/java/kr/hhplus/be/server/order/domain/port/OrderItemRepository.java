package kr.hhplus.be.server.order.domain.port;

import kr.hhplus.be.server.order.domain.OrderItem;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderItemRepository {
    Optional<OrderItem> findById(long orderItemId);
    OrderItem insertOrUpdate(OrderItem orderItem);
    List<Map.Entry<Long, Long>> findTopSellingProductIds(List<Long> orderIds, int limit);
}
