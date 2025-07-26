package kr.hhplus.be.server.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.coupon.application.CouponService;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import kr.hhplus.be.server.coupon.domain.CouponStatus;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import kr.hhplus.be.server.order.application.dto.OrderItemRequestDto;
import kr.hhplus.be.server.order.application.dto.OrderRequestDto;
import kr.hhplus.be.server.order.application.dto.OrderResponseDto;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.OrderItem;
import kr.hhplus.be.server.order.domain.port.OrderRepository;
import kr.hhplus.be.server.product.application.ProductService;
import kr.hhplus.be.server.product.application.dto.ProductResponseDto;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.exception.ProductDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderFacadeTest 테스트")
public class OrderFacadeTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CouponService couponService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderFacadeAdapter orderFacade;

    @Nested
    @DisplayName("주문 및 결제 요청 시")
    class 주문_및_결제_요청_시{

        @Test
        @DisplayName("정상적인 요청인 경우 성공한다")
        void 정상적인_요청인_경우_성공(){
            // given
            long userId = 1L;
            long couponPolicyId = 10L;
            long userCouponId = 10L;
            long productId = 100L;
            int quantity = 2;
            BigDecimal price = BigDecimal.valueOf(1000);
            BigDecimal discountRate = BigDecimal.valueOf(0.1);

            OrderItemRequestDto itemDto = new OrderItemRequestDto(productId, quantity);
            OrderRequestDto requestDto = new OrderRequestDto(userId, userCouponId, List.of(itemDto));

            Product product = Product.builder()
                .id(productId)
                .price(price)
                .stock(10)
                .build();

            Mockito.when(productService.decreaseProductStocks(List.of(itemDto)))
                .thenReturn(List.of(product));

            UserCoupon userCoupon = UserCoupon.builder()
                .id(userCouponId)
                .couponPolicyId(couponPolicyId)
                .userId(userId)
                .issuedAt(null)
                .status(CouponStatus.ISSUED)
                .build();

            Mockito.when(couponService.getCouponDomain(userCouponId)).thenReturn(userCoupon);

            CouponPolicy policy = CouponPolicy.builder()
                .id(couponPolicyId)
                .discountRate(discountRate)
                .totalCount(100)
                .remainingCount(10)
                .build();

            Mockito.when(couponService.getCouponPolicyDomain(couponPolicyId)).thenReturn(policy);

            OrderItem orderItem = OrderItem.builder()
                .productId(productId)
                .productPrice(price)
                .quantity(quantity)
                .build();

            Order expectedOrder = Order.create(userId, userCouponId, discountRate, List.of(orderItem));

            Mockito.when(orderRepository.insertOrUpdate(any(Order.class)))
                .thenReturn(expectedOrder);

            // When
            OrderResponseDto result = orderFacade.placeOrderWithPayment(requestDto);

            // Then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.userId()).isEqualTo(userId),
                () -> assertThat(result.orderItems()).hasSize(expectedOrder.getOrderItems().size())
            );
        }

        @Test
        @DisplayName("상품 재고가 부족한 경우 예외를 던진다")
        void 상품_재고_부족_시_예외() {
            long userId = 1L;
            long productId = 100L;
            int quantity = 10;

            OrderRequestDto request = new OrderRequestDto(
                userId,
                0L,
                List.of(new OrderItemRequestDto(productId, quantity))
            );

            assertThatThrownBy(() -> orderFacade.placeOrderWithPayment(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);

        }
    }
}
