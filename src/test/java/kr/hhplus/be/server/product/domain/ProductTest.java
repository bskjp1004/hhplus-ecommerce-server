package kr.hhplus.be.server.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import kr.hhplus.be.server.product.domain.exception.ProductDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Product 테스트")
public class ProductTest {

    @Nested
    @DisplayName("상품 재고 차감 시")
    class 상품_재고_차감_시{

        @Test
        @DisplayName("요청 수량이 양수면 성공한다")
        void 요청_수량이_양수면_성공(){
            Integer originalStock = 20;
            Integer requestStock = 2;
            Integer updatedStock = originalStock - requestStock;
            Product product = new Product(1, BigDecimal.valueOf(1000), originalStock);

            Product updatedProduct = product.decreaseStock(requestStock);

            assertAll(
                ()->assertThat(updatedProduct).isNotNull(),
                ()->assertThat(updatedProduct.getStock()).isEqualTo(updatedStock)
            );
        }

        @ParameterizedTest
        @DisplayName("요청 수량이 음수나 0이면 실패힌다")
        @CsvSource({
            "-1",
            "0"
        })
        void 요청_수량이_음수나_0이면_실패(Integer requestStock){
            Integer originalStock = 20;
            Product product = new Product(1, BigDecimal.valueOf(1000), originalStock);

            assertThatThrownBy(() -> product.decreaseStock(requestStock))
                .isInstanceOf(ProductDomainException.IllegalStockException.class);
        }

        @Test
        @DisplayName("요청 수량이 보유 재고 수량을 넘어가면 실패한다")
        void 요청_수량이_보유_재고_수량을_넘어가면_실패(){
            Integer requestStock = 21;
            Product product = new Product(1, BigDecimal.valueOf(1000), 20);

            assertThatThrownBy(() -> product.decreaseStock(requestStock))
                .isInstanceOf(ProductDomainException.InsufficientStockException.class);
        }
    }
}
