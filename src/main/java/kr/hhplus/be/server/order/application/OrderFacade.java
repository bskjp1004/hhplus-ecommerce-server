package kr.hhplus.be.server.order.application;

import java.math.BigDecimal;
import java.util.List;
import kr.hhplus.be.server.coupon.application.CouponService;
import kr.hhplus.be.server.order.application.dto.CreateOrderCommand;
import kr.hhplus.be.server.order.application.dto.OrderResult;
import kr.hhplus.be.server.order.domain.OrderItem;
import kr.hhplus.be.server.product.application.ProductService;
import kr.hhplus.be.server.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final CouponService couponService;
    private final ProductService productService;

    @Transactional
    public OrderResult placeOrderWithPayment(CreateOrderCommand command) {
        // 1. 쿠폰 처리
        BigDecimal discountRate = couponService.applyCouponForOrder(command.couponId());
        
        // 2. 상품 재고 차감
        List<Product> persistedProducts = productService.decreaseProductStocks(command.OrderItemCommands());
        
        // 3. OrderItem 리스트 변환
        List<OrderItem> orderItems = orderService.createOrderItems(command.OrderItemCommands(), persistedProducts);
        
        // 4. OrderService를 통한 주문 생성
        return orderService.createOrder(command, discountRate, orderItems);
    }
}
