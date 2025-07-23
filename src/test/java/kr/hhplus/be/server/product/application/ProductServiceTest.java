package kr.hhplus.be.server.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.Optional;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.product.application.dto.ProductResponseDto;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductRepository;
import kr.hhplus.be.server.product.infra.ProductInMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("ProductService 테스트")
public class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp(){
        productRepository = Mockito.mock(ProductInMemoryRepository.class);
        productService = new ProductServiceAdapter(productRepository);
    }

    @Nested
    @DisplayName("상품 조회 시")
    class 상품_조회_시{

        @Test
        @DisplayName("상품이 존재하면 정상적으로 조회된다")
        void 상품_존재_시_조회_성공(){
            long productId = 1L;
            Product mockProduct = Product.builder()
                .id(productId)
                .price(BigDecimal.ZERO)
                .stock(0L)
                .build();
            Mockito.when(productRepository.findById(productId))
                .thenReturn(Optional.of(mockProduct));

            ProductResponseDto product = productService.getProduct(productId);

            assertAll(
                ()->assertThat(product).isNotNull(),
                ()->assertThat(product.id()).isEqualTo(productId)
            );
        }

        @Test
        @DisplayName("상품이 없으면 예외를 던진다")
        void 상품_없을_때_조회_실패(){
            long productId = 1L;
            Mockito.when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                productService.getProduct(productId);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
