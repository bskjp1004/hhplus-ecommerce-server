package kr.hhplus.be.server.order.application;

import java.math.BigDecimal;
import java.util.List;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import kr.hhplus.be.server.order.application.dto.OrderItemRequestDto;
import kr.hhplus.be.server.order.application.dto.OrderRequestDto;
import kr.hhplus.be.server.order.application.dto.OrderResponseDto;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.OrderItem;
import kr.hhplus.be.server.order.domain.port.OrderRepository;
import kr.hhplus.be.server.coupon.application.CouponService;
import kr.hhplus.be.server.product.application.ProductService;
import kr.hhplus.be.server.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderFacadeAdapter implements OrderFacade {

    private final OrderRepository orderRepository;
    private final CouponService couponService;
    private final ProductService productService;

    @Override
    public OrderResponseDto placeOrderWithPayment(OrderRequestDto orderRequestDto) {
        long couponId = orderRequestDto.couponId();
        BigDecimal discountRate = BigDecimal.ZERO;

        if (couponId > 0){
            UserCoupon userCoupon = couponService.getCouponDomain(orderRequestDto.couponId());
            couponService.useCoupon(userCoupon.getId());

            CouponPolicy couponPolicy = couponService.getCouponPolicyDomain(userCoupon.getCouponPolicyId());
            discountRate = couponPolicy.getDiscountRate();
        }
        List<OrderItemRequestDto> orderItemRequests  = orderRequestDto.orderItems();

        List<Product> persistedProducts = productService.decreaseProductStocks(orderItemRequests );

        // OrderItem 리스트 변환
        List<OrderItem> orderItems = orderItemRequests .stream()
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

        // Order 생성
        Order order = Order.create(
            orderRequestDto.userId(),
            couponId,
            discountRate,
            orderItems
        );

        Order persistedOrder = orderRepository.insertOrUpdate(order);

        return OrderResponseDto.from(persistedOrder);
    }
}
