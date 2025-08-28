package kr.hhplus.be.server.order.application;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.hhplus.be.server.config.redis.RedisKey;
import kr.hhplus.be.server.config.redis.lock.DistributedLock;
import kr.hhplus.be.server.config.redis.lock.LockType;
import kr.hhplus.be.server.coupon.application.CouponService;
import kr.hhplus.be.server.order.application.dto.CreateOrderCommand;
import kr.hhplus.be.server.order.application.dto.OrderResult;
import kr.hhplus.be.server.order.domain.OrderItem;
import kr.hhplus.be.server.order.domain.event.OrderPlacedEvent;
import kr.hhplus.be.server.order.domain.event.OrderPlacedEvent.Item;
import kr.hhplus.be.server.product.application.ProductService;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.user.application.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final CouponService couponService;
    private final ProductService productService;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher events;

    @DistributedLock(lockType = LockType.ORDER, keys = {
        "#command.OrderItemCommands().![productId]"
    })
    @Transactional
    public OrderResult placeOrderWithPayment(CreateOrderCommand command) {
        // 1. 쿠폰 처리
        BigDecimal discountRate = BigDecimal.ZERO;
        if (command.couponId() != null){
            discountRate = couponService.applyCouponForOrder(command.couponId());
        }

        // 2. 주문 총액 사전 계산 및 잔액 검증
        BigDecimal totalAmount = productService.calculateTotalAmount(command.OrderItemCommands(), discountRate);
        userService.validateBalance(command.userId(), totalAmount);
        
        // 3. 상품 재고 차감
        List<Product> persistedProducts = productService.decreaseProductStocks(command.OrderItemCommands());
        
        // 4. OrderItem 리스트 변환
        List<OrderItem> orderItems = orderService.createOrderItems(command.OrderItemCommands(), persistedProducts);
        
        // 5. OrderService를 통한 주문 생성
        OrderResult orderResult = orderService.createOrder(command, discountRate, orderItems);

        // 6. 유저 잔액 차감 (이미 검증됨)
        userService.useBalance(command.userId(), orderResult.paidPrice());

        // 7. Redis에 날짜별 상품 랭킹 저장
        addProductScoreToRedis(orderItems);

        // 8. 주문정보 외부 플랫폼 전송 이벤트 발행
        var items = orderResult.orderItems().stream()
            .map(oi -> new Item(
                oi.productId(),
                oi.quantity(),
                oi.unitPrice()
            ))
            .toList();

        events.publishEvent(OrderPlacedEvent.of(
            orderResult.id(),
            orderResult.userId(),
            orderResult.paidPrice(),
            items
        ));

        return orderResult;
    }

    private void addProductScoreToRedis(List<OrderItem> orderItems){
        LocalDateTime today = LocalDateTime.now();
        String todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String dailyKey = RedisKey.PRODUCT_RANK_DAILY.key(todayStr);
        Duration ttl = RedisKey.PRODUCT_RANK_DAILY.ttlFromNow(today);

        try {
            if (!redisTemplate.hasKey(dailyKey)) {
                redisTemplate.expire(dailyKey, ttl);
            }

            for (OrderItem item : orderItems) {
                redisTemplate.opsForZSet()
                    .incrementScore(dailyKey,
                        item.getProductId(),
                        item.getQuantity());
            }

        } catch (Exception e) {
            log.warn("Failed to update Redis: {}", e.getMessage());
        }

    }

}
