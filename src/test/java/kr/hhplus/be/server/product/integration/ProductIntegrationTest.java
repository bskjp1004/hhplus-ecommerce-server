package kr.hhplus.be.server.product.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.OrderItem;
import kr.hhplus.be.server.order.domain.port.OrderRepository;
import kr.hhplus.be.server.product.application.ProductFacade;
import kr.hhplus.be.server.product.application.ProductService;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.port.ProductRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.domain.port.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("상품 통합 테스트")
public class ProductIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Nested
    @DisplayName("성공 시나리오 테스트")
    class 성공_시나리오_테스트 {

        @Test
        @DisplayName("상품을 조회할 수 있다")
        void 상품을_조회할_수_있다() {
            // given
            Product product = productRepository.insertOrUpdate(
                Product.builder()
                    .price(BigDecimal.valueOf(5_000))
                    .stock(10)
                    .build()
            );

            var result = productService.getProduct(product.getId());

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.id()).isEqualTo(product.getId())
            );
        }

        @Test
        @DisplayName("인기상품을 조회할 수 있다")
        void 인기상품을_조회할_수_있다(){
            // given
            // 사용자 생성
            User user = userRepository.insertOrUpdate(
                User.builder()
                    .balance(BigDecimal.valueOf(20_000))
                    .build()
            );

            // 상품 10개 생성
            List<Long> prodcutIds = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Product product = productRepository.insertOrUpdate(
                    Product.builder()
                        .price(BigDecimal.valueOf(5_000))
                        .stock(10)
                        .build()
                );

                prodcutIds.add(product.getId());
            }

            // 주문 생성
            for (int i = 0; i< 10; i++) {

                OrderItem orderItem = OrderItem.builder()
                    .productId(prodcutIds.get(i))
                    .quantity(++i)
                    .productPrice(BigDecimal.valueOf(5_000))
                    .build();

                Order order = Order.create(
                    user.getId(),
                    null,
                    BigDecimal.valueOf(0),
                    List.of(orderItem)
                );

                orderRepository.insertOrUpdate(order);
            }

            // when
            var result = productFacade.getTopSellingProducts();

            // then
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.size()).isEqualTo(5)
            );
        }
    }

    @Nested
    @DisplayName("실패 시나리오 테스트")
    class 실패_시나리오_테스트 {

        @Test
        @DisplayName("존재하지 않는 상품은 조회할 수 없다")
        void 존재하지_않는_상품은_조회할_수_없다() {
            // when & then
            assertThatThrownBy(() -> productService.getProduct(999999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
