package kr.hhplus.be.server.order.application;

import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.order.application.dto.CreateOrderCommand;
import kr.hhplus.be.server.order.application.dto.OrderResult;
import kr.hhplus.be.server.order.application.dto.OrderItemCommand;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.OrderItem;
import kr.hhplus.be.server.order.domain.port.OrderItemRepository;
import kr.hhplus.be.server.order.domain.port.OrderRepository;
import kr.hhplus.be.server.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public OrderResult createOrder(CreateOrderCommand command, BigDecimal discountRate, List<OrderItem> orderItems) {
        Order order = Order.create(
            command.userId(),
            command.couponId(),
            discountRate,
            orderItems
        );
        
        Order savedOrder = orderRepository.insertOrUpdate(order);
        
        return OrderResult.from(savedOrder);
    }

    public List<OrderItem> createOrderItems(List<OrderItemCommand> orderItemCommands,
                                          List<Product> persistedProducts) {
        return orderItemCommands.stream()
            .map(dto -> {
                Product product = persistedProducts.stream()
                    .filter(p -> p.getId() == dto.productId())
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

                return OrderItem.builder()
                    .productId(product.getId())
                    .productPrice(product.getPrice())
                    .quantity(dto.quantity())
                    .build();
            })
            .toList();
    }
    
    @Transactional(readOnly = true)
    public List<Long> getTopSellingProductIds(int days, int limit) {
        // 1. 최근 N일간의 주문 ID 조회
        LocalDateTime targetDate = LocalDateTime.now().minusDays(days);
        List<Long> recentOrderIds = orderRepository.findOrderIdsByOrderedAtAfter(targetDate);
        
        if (recentOrderIds.isEmpty()) {
            return List.of();
        }
        
        // 2. 해당 주문들에서 가장 많이 팔린 상품 ID 조회
        List<Map.Entry<Long, Long>> topSellingProducts = orderItemRepository.findTopSellingProductIds(recentOrderIds, limit);
        
        // 3. 상품 ID만 추출하여 반환
        return topSellingProducts.stream()
            .map(Map.Entry::getKey)
            .toList();
    }
}
