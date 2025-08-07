package kr.hhplus.be.server.order.application;

import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.order.application.dto.CreateOrderCommand;
import kr.hhplus.be.server.order.application.dto.OrderResult;
import kr.hhplus.be.server.order.application.dto.OrderItemCommand;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.OrderItem;
import kr.hhplus.be.server.order.domain.port.OrderRepository;
import kr.hhplus.be.server.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

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
}
