package kr.hhplus.be.server.product.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.config.redis.RedisKey;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.OrderItem;
import kr.hhplus.be.server.order.domain.port.OrderRepository;
import kr.hhplus.be.server.product.application.ProductFacade;
import kr.hhplus.be.server.product.application.ProductService;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.port.ProductRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

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
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @BeforeEach
    void setUp() {
        // Redis ZSET 초기화 - zs:product로 시작하는 모든 키 삭제
        redisTemplate.keys("*zs:product*").forEach(key -> redisTemplate.delete(key));
        // 캐시 초기화
        redisTemplate.keys("*cache:product*").forEach(key -> redisTemplate.delete(key));
    }

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
        
        @Test
        @DisplayName("Redis ZSET 기반 인기 상품 조회 테스트")
        void Redis_ZSET_기반_인기_상품_조회_테스트() {
            // Given: DB에 상품 10개 생성
            List<Product> products = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                Product product = productRepository.insertOrUpdate(
                    Product.builder()
                        .price(BigDecimal.valueOf(10000 + (i * 1000)))
                        .stock(100)
                        .build()
                );
                products.add(product);
            }
            
            // Redis ZSET에 판매 데이터 삽입 (최근 3일간 데이터)
            for (int day = 0; day < 3; day++) {
                LocalDate date = LocalDate.now().minusDays(day);
                String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                String dailyKey = RedisKey.PRODUCT_RANK_DAILY.key(dateStr);
                Duration ttl = RedisKey.PRODUCT_RANK_DAILY.ttlFromNow(LocalDateTime.now().minusDays(day));
                
                // 상위 5개 상품에 높은 점수 부여
                redisTemplate.opsForZSet().incrementScore(dailyKey, products.get(0).getId(), 100 - (day * 10));
                redisTemplate.opsForZSet().incrementScore(dailyKey, products.get(1).getId(), 90 - (day * 10));
                redisTemplate.opsForZSet().incrementScore(dailyKey, products.get(2).getId(), 80 - (day * 10));
                redisTemplate.opsForZSet().incrementScore(dailyKey, products.get(3).getId(), 70 - (day * 10));
                redisTemplate.opsForZSet().incrementScore(dailyKey, products.get(4).getId(), 60 - (day * 10));
                
                // 나머지 상품에 낮은 점수
                for (int i = 5; i < products.size(); i++) {
                    redisTemplate.opsForZSet().incrementScore(dailyKey, products.get(i).getId(), 10 - i);
                }
                
                // TTL 설정
                redisTemplate.expire(dailyKey, ttl);
            }
            
            // When: Redis ZSET 기반 인기 상품 조회
            var result = productFacade.getTopSellingProductsFromRedis();
            
            // Then: 검증
            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.size()).isEqualTo(5),
                () -> assertThat(result.get(0).id()).isEqualTo(products.get(0).getId()),
                () -> assertThat(result.get(1).id()).isEqualTo(products.get(1).getId()),
                () -> assertThat(result.get(2).id()).isEqualTo(products.get(2).getId()),
                () -> assertThat(result.get(3).id()).isEqualTo(products.get(3).getId()),
                () -> assertThat(result.get(4).id()).isEqualTo(products.get(4).getId())
            );
            
            // 캐시 적용 확인 - 두 번째 호출
            var cachedResult = productFacade.getTopSellingProductsFromRedis();
            
            assertAll(
                () -> assertThat(cachedResult).isNotNull(),
                () -> assertThat(cachedResult.size()).isEqualTo(5),
                () -> assertThat(cachedResult).isEqualTo(result) // 캐시된 결과와 동일해야 함
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
