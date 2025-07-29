package kr.hhplus.be.server.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.util.List;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.coupon.application.CouponService;
import kr.hhplus.be.server.order.application.dto.OrderResult;
import kr.hhplus.be.server.order.application.dto.CreateOrderCommand;
import kr.hhplus.be.server.order.application.dto.OrderItemCommand;
import kr.hhplus.be.server.order.domain.OrderItem;
import kr.hhplus.be.server.product.application.ProductService;
import kr.hhplus.be.server.product.domain.Product;
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
    private OrderService orderService;

    @Mock
    private CouponService couponService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderFacade orderFacade;

    @Nested
    @DisplayName("주문 및 결제 요청 시")
    class 주문_및_결제_요청_시{

        @Test
        @DisplayName("정상적인 요청인 경우 성공한다")
        void 정상적인_요청인_경우_성공(){
            // given
            long userId = 1L;
            long userCouponId = 10L;
            long productId = 100L;
            int quantity = 2;
            BigDecimal price = BigDecimal.valueOf(1000);
            BigDecimal discountRate = BigDecimal.valueOf(0.1);

            var itemCommand = new OrderItemCommand(productId, quantity);
            var command = new CreateOrderCommand(userId, userCouponId, List.of(itemCommand));

            Product product = Product.builder()
                .id(productId)
                .price(price)
                .stock(10)
                .build();

            OrderItem orderItem = OrderItem.builder()
                .productId(productId)
                .productPrice(price)
                .quantity(quantity)
                .build();

            OrderResult expectedResult = new OrderResult(
                1L, userId, userCouponId, null, 
                BigDecimal.valueOf(2000), discountRate, BigDecimal.valueOf(1800), 
                List.of()
            );

            // mock 설정
            Mockito.when(couponService.applyCouponForOrder(userCouponId))
                .thenReturn(discountRate);
            
            Mockito.when(productService.decreaseProductStocks(List.of(itemCommand)))
                .thenReturn(List.of(product));
            
            Mockito.when(orderService.createOrderItems(List.of(itemCommand), List.of(product)))
                .thenReturn(List.of(orderItem));
            
            Mockito.when(orderService.createOrder(any(), any(), any()))
                .thenReturn(expectedResult);

            // when
            OrderResult result = orderFacade.placeOrderWithPayment(command);

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.userId()).isEqualTo(userId),
                () -> assertThat(result).isEqualTo(expectedResult)
            );
        }

        @Test
        @DisplayName("상품 재고가 부족한 경우 예외를 던진다")
        void 상품_재고_부족_시_예외() {
            // given
            long userId = 1L;
            long productId = 100L;
            int quantity = 10;

            var itemCommand = new OrderItemCommand(productId, quantity);
            var command = new CreateOrderCommand(
                userId,
                0L,
                List.of(itemCommand)
            );

            // mock 설정: 쿠폰 처리는 성공하지만 상품 재고 차감에서 실패
            Mockito.when(couponService.applyCouponForOrder(0L))
                .thenReturn(BigDecimal.ZERO);
            
            Mockito.when(productService.decreaseProductStocks(List.of(itemCommand)))
                .thenThrow(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> orderFacade.placeOrderWithPayment(command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
