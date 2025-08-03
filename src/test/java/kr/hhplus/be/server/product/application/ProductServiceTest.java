package kr.hhplus.be.server.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.Optional;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.order.application.dto.OrderItemCommand;
import kr.hhplus.be.server.product.application.dto.ProductResponseDto;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.port.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 테스트")
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

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
                .stock(0)
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

    @Nested
    @DisplayName("상품 재고 차감 시")
    class 상품_재고_차감_시{
        @Test
        @DisplayName("정상적으로 차감된다")
        void 정상적으로_차감(){
            long productId = 1L;
            Integer originalStock = 20;
            Integer requestStock = 2;
            Integer updatedStock = originalStock - requestStock;
            var requests = new OrderItemCommand(productId, requestStock);
            Product mockProduct = Product.builder()
                .id(productId)
                .price(BigDecimal.valueOf(2000))
                .stock(originalStock)
                .build();

            Mockito.when(productRepository.findById(productId))
                .thenReturn(Optional.of(mockProduct));
            Mockito.when(productRepository.insertOrUpdate(Mockito.any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            Product productResponseDto = productService.decreaseProductStock(requests);

            assertAll(
                ()->assertThat(productResponseDto).isNotNull(),
                ()->assertThat(productResponseDto.getStock()).isEqualTo(updatedStock)
            );
        }

        @Test
        @DisplayName("상품이 존재하지 않으면 실패한다")
        void 상품이_존재하지_않으면_실패(){
            long productId = 1L;
            Integer requestStock = 2;
            var requests = new OrderItemCommand(productId, requestStock);
            Mockito.when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.decreaseProductStock(requests))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
