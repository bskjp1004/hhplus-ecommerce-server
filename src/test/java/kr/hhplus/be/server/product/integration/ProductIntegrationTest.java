package kr.hhplus.be.server.product.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.product.application.ProductService;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.port.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("상품 통합 테스트")
public class ProductIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

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
    }

    @Nested
    @DisplayName("실패 시나리오 테스트")
    class 실패_시나리오_테스트 {

        @Test
        @DisplayName("존재하지 않는 상품은 조회할 수 없다")
        void 존재하지_않는_상품은_조회할_수_없다() {
            // when & then
            assertThatThrownBy(() -> productService.getProduct(1))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
