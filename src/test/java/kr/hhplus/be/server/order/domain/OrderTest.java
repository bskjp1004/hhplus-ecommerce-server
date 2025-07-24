package kr.hhplus.be.server.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import kr.hhplus.be.server.order.domain.exception.OrderDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Order 테스트")
public class OrderTest {

    @Nested
    @DisplayName("주문 생성 할 때")
    class 주문_생성_할_때{

        @Test
        @DisplayName("정상적인 요청이면 성공한다")
        void 정상적인_요청이면_성공(){
            long orderId = 1L;
            long userId = 1L;
            long couponId = 1L;
            BigDecimal discountRate = BigDecimal.valueOf(0.2);
            List<OrderItem> orderItems = new ArrayList<>();
            orderItems.add(new OrderItem(1L, orderId, 1L, 1, BigDecimal.valueOf(5000)));
            orderItems.add(new OrderItem(2L, orderId, 2L, 1, BigDecimal.valueOf(5000)));

            Order order = Order.create(userId, couponId, discountRate, orderItems);

            assertAll(
                () -> assertThat(order).isNotNull(),
                () -> assertThat(order.getPaidPrice()).isEqualByComparingTo(BigDecimal.valueOf(8000)),
                () -> assertThat(order.getOrderItems()).hasSize(orderItems.size())
            );
        }

        @Test
        @DisplayName("주문 할 상품이 비어있으면 실패한다")
        void 주문_할_상품이_비어있으면_실패(){
            long userId = 1L;
            long couponId = 1L;
            BigDecimal discountRate = BigDecimal.valueOf(0.2);
            List<OrderItem> orderItems = new ArrayList<>();

            assertThatThrownBy(() -> Order.create(userId, couponId, discountRate, orderItems))
                .isInstanceOf(OrderDomainException.EmptyOrderItemsException.class);
        }
    }
}
