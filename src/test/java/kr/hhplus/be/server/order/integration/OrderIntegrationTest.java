package kr.hhplus.be.server.order.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.List;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import kr.hhplus.be.server.coupon.domain.exception.CouponDomainException;
import kr.hhplus.be.server.coupon.domain.port.CouponPolicyRepository;
import kr.hhplus.be.server.coupon.domain.port.UserCouponRepository;
import kr.hhplus.be.server.order.application.OrderFacade;
import kr.hhplus.be.server.order.application.dto.CreateOrderCommand;
import kr.hhplus.be.server.order.application.dto.OrderItemCommand;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.port.OrderRepository;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.exception.ProductDomainException;
import kr.hhplus.be.server.product.domain.port.ProductRepository;
import kr.hhplus.be.server.BaseIntegrationTest;
import kr.hhplus.be.server.user.domain.BalanceHistory;
import kr.hhplus.be.server.user.domain.BalanceStatus;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.domain.exception.UserDomainException;
import kr.hhplus.be.server.user.domain.port.BalanceHistoryRepository;
import kr.hhplus.be.server.user.domain.port.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

@DisplayName("주문 통합 테스트")
class OrderIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BalanceHistoryRepository balanceHistoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CouponPolicyRepository couponPolicyRepository;
    
    @Autowired
    private UserCouponRepository userCouponRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Nested
    @DisplayName("성공 시나리오 테스트")
    class 성공_시나리오_테스트 {
        
        @Test
        @DisplayName("쿠폰을 적용하여 상품을 주문할 수 있다")
        void 쿠폰을_적용하여_상품을_주문할_수_있다() {
            // given
            // 1. 사용자 생성 - 잔액 20,000원
            User user = userRepository.insertOrUpdate(
                User.builder()
                    .balance(BigDecimal.valueOf(20_000))
                    .build()
            );
            BalanceHistory balanceHistory = balanceHistoryRepository.insertOrUpdate(
                BalanceHistory.create(user.getId(), BalanceStatus.CHARGE, BigDecimal.valueOf(20_000))
            );
            
            // 2. 상품 생성 - A상품 가격 5,000원, 재고 10개 / B상품 가격 10,000원, 재고 10개
            Product productA = productRepository.insertOrUpdate(
                Product.builder()
                    .price(BigDecimal.valueOf(5_000))
                    .stock(10)
                    .build()
            );
            Product productB = productRepository.insertOrUpdate(
                Product.builder()
                    .price(BigDecimal.valueOf(10_000))
                    .stock(10)
                    .build()
            );
            
            // 3. 쿠폰 정책 생성 - 10% 할인, 총 100개, 잔여 50개
            CouponPolicy couponPolicy = couponPolicyRepository.insertOrUpdate(
                CouponPolicy.builder()
                    .discountRate(BigDecimal.valueOf(0.1))
                    .totalCount(100)
                    .remainingCount(50)
                    .build()
            );
            
            // 4. 사용자 쿠폰 생성
            UserCoupon createdUserCoupon = UserCoupon.create(couponPolicy.getId(), user.getId());
            
            UserCoupon userCoupon = userCouponRepository.insertOrUpdate(createdUserCoupon);
            
            // 5. 주문 요청 생성
            CreateOrderCommand command = new CreateOrderCommand(
                user.getId(),
                userCoupon.getId(),
                List.of(
                    new OrderItemCommand(productA.getId(), 2),
                    new OrderItemCommand(productB.getId(), 1)
                )
            );
            
            // when
            var orderResult = orderFacade.placeOrderWithPayment(command);
            
            // then
            assertAll(
                // 주문 검증
                () -> assertThat(orderResult).isNotNull(),
                () -> assertThat(orderResult.userId()).isEqualTo(user.getId()),
                () -> assertThat(orderResult.discountRate()).isEqualByComparingTo(BigDecimal.valueOf(0.1)),
                () -> assertThat(orderResult.paidPrice()).isEqualByComparingTo(BigDecimal.valueOf(18_000)),
                
                // 사용자 잔액 검증
                () -> {
                    User updatedUser = userRepository.findById(user.getId()).orElseThrow();
                    assertThat(updatedUser.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(2_000));
                },
                () -> {
                    List<BalanceHistory> updatedUser = balanceHistoryRepository.findAllByUserId(user.getId());
                    assertThat(updatedUser.size()).isEqualTo(2);
                },
                
                // 상품 재고 검증
                () -> {
                    Product updatedProduct = productRepository.findById(productA.getId()).orElseThrow();
                    assertThat(updatedProduct.getStock()).isEqualByComparingTo(8);
                },
                () -> {
                    Product updatedProduct = productRepository.findById(productB.getId()).orElseThrow();
                    assertThat(updatedProduct.getStock()).isEqualByComparingTo(9);
                },
                
                // 쿠폰 사용 여부 검증
                () -> {
                    UserCoupon usedCoupon = userCouponRepository.findById(userCoupon.getId()).orElseThrow();
                    assertThat(usedCoupon.canUse()).isFalse();
                },
                
                // 주문 저장 검증
                () -> {
                    Order savedOrder = orderRepository.findById(orderResult.id()).orElseThrow();
                    assertThat(savedOrder).isNotNull();
                    assertThat(savedOrder.getOrderItems()).hasSize(2);
                }
            );
        }
        
        @Test
        @DisplayName("쿠폰 없이 상품을 주문할 수 있다")
        void 쿠폰_없이_상품을_주문할_수_있다() {
            // given
            // 1. 사용자 생성 - 잔액 10,000원
            User user = userRepository.insertOrUpdate(
                User.builder()
                    .balance(BigDecimal.valueOf(10_000))
                    .build()
            );

            // 2. 상품 생성 - 가격 5,000원, 재고 10개
            Product product = productRepository.insertOrUpdate(
                Product.builder()
                    .price(BigDecimal.valueOf(5_000))
                    .stock(10)
                    .build()
            );

            // 3. 주문 요청 생성
            CreateOrderCommand command = new CreateOrderCommand(
                user.getId(),
                null,
                List.of(new OrderItemCommand(product.getId(), 2))
            );

            // when
            var orderResult = orderFacade.placeOrderWithPayment(command);

            // then
            assertAll(
                // 주문 검증
                () -> assertThat(orderResult).isNotNull(),
                () -> assertThat(orderResult.userId()).isEqualTo(user.getId()),
                () -> assertThat(orderResult.discountRate()).isEqualByComparingTo(BigDecimal.ZERO),
                () -> assertThat(orderResult.paidPrice()).isEqualByComparingTo(BigDecimal.valueOf(10_000)),

                // 사용자 잔액 검증
                () -> {
                    User updatedUser = userRepository.findById(user.getId()).orElseThrow();
                    assertThat(updatedUser.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(0));
                },

                // 상품 재고 검증
                () -> {
                    Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
                    assertThat(updatedProduct.getStock()).isEqualByComparingTo(8);
                },

                // 주문 저장 검증
                () -> {
                    Order savedOrder = orderRepository.findById(orderResult.id()).orElseThrow();
                    assertThat(savedOrder).isNotNull();
                    assertThat(savedOrder.getOrderItems()).hasSize(1);
                }
            );
        }
    }
    
    @Nested
    @DisplayName("실패 시나리오 테스트")
    class 실패_시나리오_테스트 {

        @Test
        @DisplayName("사용된 쿠폰은 재사용할 수 없다")
        void 사용된_쿠폰은_재사용할_수_없다() {
            // given
            // 1. 사용자 생성 - 잔액 20,000원
            User user = userRepository.insertOrUpdate(
                User.builder()
                    .balance(BigDecimal.valueOf(20_000))
                    .build()
            );

            // 2. 상품 생성 - A상품 가격 5,000원, 재고 10개 / B상품 가격 10,000원, 재고 10개
            Product productA = productRepository.insertOrUpdate(
                Product.builder()
                    .price(BigDecimal.valueOf(5_000))
                    .stock(10)
                    .build()
            );
            Product productB = productRepository.insertOrUpdate(
                Product.builder()
                    .price(BigDecimal.valueOf(10_000))
                    .stock(10)
                    .build()
            );

            // 3. 쿠폰 정책 생성 - 10% 할인, 총 100개, 잔여 50개
            CouponPolicy couponPolicy = couponPolicyRepository.insertOrUpdate(
                CouponPolicy.builder()
                    .discountRate(BigDecimal.valueOf(0.1))
                    .totalCount(100)
                    .remainingCount(50)
                    .build()
            );

            // 4. 이미 사용된 쿠폰 생성
            UserCoupon createdUserCoupon = UserCoupon.create(couponPolicy.getId(), user.getId());
            createdUserCoupon = createdUserCoupon.useCoupon();
            UserCoupon userCoupon = userCouponRepository.insertOrUpdate(createdUserCoupon);

            // 5. 주문 요청 생성
            CreateOrderCommand command = new CreateOrderCommand(
                user.getId(),
                userCoupon.getId(),
                List.of(
                    new OrderItemCommand(productA.getId(), 2),
                    new OrderItemCommand(productB.getId(), 1)
                )
            );

            // when & then
            assertThatThrownBy(() -> orderFacade.placeOrderWithPayment(command))
                .isInstanceOf(CouponDomainException.AlreadyUsedCouponException.class);
        }

        @Test
        @DisplayName("사용자 잔액이 부족하면 주문이 실패한다")
        void 사용자_잔액이_부족하면_주문이_실패한다() {
            // given
            // 1. 사용자 생성 - 잔액 10,000원
            User user = userRepository.insertOrUpdate(
                User.builder()
                    .balance(BigDecimal.valueOf(10_000))
                    .build()
            );

            // 2. 상품 생성 - A상품 가격 5,000원, 재고 10개 / B상품 가격 10,000원, 재고 10개
            Product productA = productRepository.insertOrUpdate(
                Product.builder()
                    .price(BigDecimal.valueOf(5_000))
                    .stock(10)
                    .build()
            );
            Product productB = productRepository.insertOrUpdate(
                Product.builder()
                    .price(BigDecimal.valueOf(10_000))
                    .stock(10)
                    .build()
            );

            // 3. 쿠폰 정책 생성 - 10% 할인, 총 100개, 잔여 50개
            CouponPolicy couponPolicy = couponPolicyRepository.insertOrUpdate(
                CouponPolicy.builder()
                    .discountRate(BigDecimal.valueOf(0.1))
                    .totalCount(100)
                    .remainingCount(50)
                    .build()
            );

            // 4. 사용자 쿠폰 생성
            UserCoupon createdUserCoupon = UserCoupon.create(couponPolicy.getId(), user.getId());

            UserCoupon userCoupon = userCouponRepository.insertOrUpdate(createdUserCoupon);

            // 5. 주문 요청 생성
            CreateOrderCommand command = new CreateOrderCommand(
                user.getId(),
                userCoupon.getId(),
                List.of(
                    new OrderItemCommand(productA.getId(), 2),
                    new OrderItemCommand(productB.getId(), 1)
                )
            );

            // when & then
            assertThatThrownBy(() -> orderFacade.placeOrderWithPayment(command))
                .isInstanceOf(UserDomainException.InsufficientBalanceException.class);
        }
        
        @Test
        @DisplayName("상품 재고가 부족하면 주문이 실패한다")
        void 상품_재고가_부족하면_주문이_실패한다() {
            // given
            // 1. 사용자 생성 - 잔액 10,000원
            User user = userRepository.insertOrUpdate(
                User.builder()
                    .balance(BigDecimal.valueOf(10_000))
                    .build()
            );

            // 2. 상품 생성 - 가격 5,000원, 재고 1개
            Product product = productRepository.insertOrUpdate(
                Product.builder()
                    .price(BigDecimal.valueOf(5_000))
                    .stock(1)
                    .build()
            );

            // 3. 주문 요청 생성 - 상품 2개
            CreateOrderCommand command = new CreateOrderCommand(
                user.getId(),
                null,
                List.of(new OrderItemCommand(product.getId(), 2))
            );

            // when & then
            assertThatThrownBy(() -> orderFacade.placeOrderWithPayment(command))
                .isInstanceOf(ProductDomainException.InsufficientStockException.class);
        }
        
        @Test
        @DisplayName("주문 처리 중 예외 발생시 모든 변경사항이 롤백된다")
        void 주문_처리_중_예외_발생시_모든_변경사항이_롤백된다() {
            // given
            // 1. 사용자 생성 - 잔액 5,000원 (부족한 금액)
            User user = userRepository.insertOrUpdate(
                User.builder()
                    .balance(BigDecimal.valueOf(5_000))
                    .build()
            );

            // 2. 상품 생성 - 가격 5,000원, 재고 10개
            Product product = productRepository.insertOrUpdate(
                Product.builder()
                    .price(BigDecimal.valueOf(5_000))
                    .stock(10)
                    .build()
            );

            // 3. 쿠폰 없이 주문 요청 생성
            CreateOrderCommand command = new CreateOrderCommand(
                user.getId(),
                null,
                List.of(
                    new OrderItemCommand(product.getId(), 3)
                )
            );

            // 상태 저장
            BigDecimal 초기_잔액 = user.getBalance();
            Integer 초기_재고 = product.getStock();
            int 초기_주문수 = orderRepository.findAllByUserId(user.getId()).size();

            // when
            assertThatThrownBy(() -> orderFacade.placeOrderWithPayment(command))
                .isInstanceOf(UserDomainException.InsufficientBalanceException.class);

            // then
            // 새로운 트랜잭션에서 롤백 검증
            transactionTemplate.execute(status -> {
                User rollbackUser = userRepository.findById(user.getId()).orElseThrow();
                Product rollbackProduct = productRepository.findById(product.getId()).orElseThrow();
                int 현재_주문수 = orderRepository.findAllByUserId(user.getId()).size();

                assertAll(
                    // 사용자 잔액이 원래대로
                    () -> {
                        assertThat(rollbackUser.getBalance()).isEqualByComparingTo(초기_잔액);
                    },

                    // 상품 재고가 원래대로
                    () -> {
                        assertThat(rollbackProduct.getStock()).isEqualTo(초기_재고);
                    },

                    // 주문이 생성되지 않음
                    () -> {
                        assertThat(현재_주문수).isEqualTo(초기_주문수);
                    }
                );
                return null;
            });
        }
    }
}