package kr.hhplus.be.server.order.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kr.hhplus.be.server.BaseConcurrencyTest;
import kr.hhplus.be.server.coupon.domain.port.CouponPolicyRepository;
import kr.hhplus.be.server.coupon.domain.port.UserCouponRepository;
import kr.hhplus.be.server.order.application.OrderFacade;
import kr.hhplus.be.server.order.application.dto.CreateOrderCommand;
import kr.hhplus.be.server.order.application.dto.OrderItemCommand;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.port.OrderRepository;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.port.ProductRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.domain.port.BalanceHistoryRepository;
import kr.hhplus.be.server.user.domain.port.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

@DisplayName("주문 동시성 제어 테스트")
public class OrderConcurrencyControllTest extends BaseConcurrencyTest {

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
    @DisplayName("동시성 제어 성공 시나리오")
    class 동시성_제어_성공_시나리오{

        @Test
        @DisplayName("동시에 주문 시 동시성 제어가 가능하다")
        void 동시에_주문_시_동시성_제어가_가능하다() throws InterruptedException {
            // given
            // 1. 사용자 생성 - 잔액 100,000원
            User user = userRepository.insertOrUpdate(
                User.builder()
                    .balance(BigDecimal.valueOf(100_000))
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
                List.of(
                    new OrderItemCommand(product.getId(), 10)
                )
            );

            // 4. 스레드 준비
            int threadCount = 2;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            // when
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        orderFacade.placeOrderWithPayment(command);
                    } catch (Exception e) {
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            doneLatch.await();
            executor.shutdown();

            // then
            assertAll(
                // 최종 재고 0이어야함
                () -> {
                    transactionTemplate.execute(status -> {
                    Product finalProduct = productRepository.findById(product.getId()).orElseThrow();
                    assertThat(finalProduct.getStock()).isEqualTo(0);
                        return null;
                    });
                },
                // 성공한 주문이 1개만 생성되어야 함
                () -> {
                    transactionTemplate.execute(status -> {
                    List<Order> orders = orderRepository.findAllByUserId(user.getId());
                    assertThat(orders).hasSize(1);
                        return null;
                    });
                }
            );
        }

        @Test
        @DisplayName("서로 다른 순서로 여러 상품 주문 시 동시성 제어가 가능하다")
        void 서로_다른_순서로_여러_상품_주문_시_동시성_제어가_가능하다() throws InterruptedException {
            // given
            // 1. 사용자 2명 생성
            User user1 = userRepository.insertOrUpdate(
                User.builder()
                    .balance(BigDecimal.valueOf(100_000))
                    .build()
            );
            User user2 = userRepository.insertOrUpdate(
                User.builder()
                    .balance(BigDecimal.valueOf(100_000))
                    .build()
            );

            // 2. 상품 2개 생성
            Product productA = productRepository.insertOrUpdate(
                Product.builder()
                    .price(BigDecimal.valueOf(5_000))
                    .stock(10)
                    .build()
            );
            Product productB = productRepository.insertOrUpdate(
                Product.builder()
                    .price(BigDecimal.valueOf(3_000))
                    .stock(10)
                    .build()
            );

            // 3. 주문 요청 생성 - 순서를 반대로
            CreateOrderCommand command1 = new CreateOrderCommand(
                user1.getId(),
                null,
                List.of(
                    new OrderItemCommand(productA.getId(), 1),
                    new OrderItemCommand(productB.getId(), 1)
                )
            );

            CreateOrderCommand command2 = new CreateOrderCommand(
                user2.getId(),
                null,
                List.of(
                    new OrderItemCommand(productB.getId(), 1),
                    new OrderItemCommand(productA.getId(), 1)
                )
            );

            // 4. 스레드 준비
            int threadCount = 2;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            // when
            executor.submit(() -> {
                try {
                    startLatch.await();

                    orderFacade.placeOrderWithPayment(command1);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });

            executor.submit(() -> {
                try {
                    startLatch.await();
                    orderFacade.placeOrderWithPayment(command2);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });

            startLatch.countDown();
            doneLatch.await();
            executor.shutdown();

            // then
            assertAll(
                // 모든 주문 성공
                () -> {
                    transactionTemplate.execute(status -> {
                    List<Order> orders1 = orderRepository.findAllByUserId(user1.getId());
                    assertThat(orders1).hasSize(1);
                        return null;
                    });
                    transactionTemplate.execute(status -> {
                    List<Order> orders2 = orderRepository.findAllByUserId(user2.getId());
                    assertThat(orders2).hasSize(1);
                        return null;
                    });
                },
                // 요청한 상품 재고 차감 성공
                () -> {
                    transactionTemplate.execute(status -> {
                    Product finalProductA = productRepository.findById(productA.getId()).orElseThrow();
                    assertThat(finalProductA.getStock()).isEqualTo(8);
                        return null;
                    });
                    transactionTemplate.execute(status -> {
                    Product finalProductB = productRepository.findById(productB.getId()).orElseThrow();
                    assertThat(finalProductB.getStock()).isEqualTo(8);
                    return null;
                });
                }
            );
        }
    }
}
