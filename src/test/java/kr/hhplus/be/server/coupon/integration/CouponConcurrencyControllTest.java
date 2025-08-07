package kr.hhplus.be.server.coupon.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import kr.hhplus.be.server.ServerApplication;
import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.coupon.application.CouponService;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import kr.hhplus.be.server.coupon.domain.port.CouponPolicyRepository;
import kr.hhplus.be.server.coupon.domain.port.UserCouponRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.domain.port.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ServerApplication.class, TestcontainersConfiguration.class })
@SpringBootTest
@DisplayName("쿠폰 동시성 제어 테스트")
public class CouponConcurrencyControllTest {
    
    @Autowired
    private CouponService couponService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponPolicyRepository couponPolicyRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("동시에 선착순 쿠폰 발급 요청 시 동시성 제어가 가능하다")
    void 동시에_선착순_쿠폰_발급_요청_시_동시성_제어가_가능하다() throws InterruptedException {
        // given
        CouponPolicy couponPolicy = couponPolicyRepository.insertOrUpdate(
                CouponPolicy.builder()
                    .discountRate(BigDecimal.valueOf(0.1))
                    .totalCount(100)
                    .remainingCount(1)
                    .build()
        );

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        List<Long> userIds = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();

                    transactionTemplate.execute(status -> {
                        try {
                            User user = userRepository.insertOrUpdate(
                                User.empty()
                            );
                            userIds.add(user.getId());

                            couponService.issueLimitedCoupon(user.getId(), couponPolicy.getId());
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                            status.setRollbackOnly();
                        }
                        return null;
                    });
                } catch (Exception e) {
                    failCount.incrementAndGet();
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
            // 성공 1개, 실패 1개
            () -> {
                assertThat(successCount.get()).isEqualTo(1);
                assertThat(failCount.get()).isEqualTo(1);
            },
            // 발급된 쿠폰은 1개
            () -> {
                List<UserCoupon> updatedUserCoupons = userCouponRepository.findAllByCouponPolicyId(couponPolicy.getId());
                assertThat(updatedUserCoupons.size()).isEqualTo(1);
            },
            // 쿠폰 정책 잔여 수량 0
            () -> {
                transactionTemplate.execute(status -> {
                CouponPolicy finalCouponPolicy = couponPolicyRepository.findById(couponPolicy.getId()).orElseThrow();
                assertThat(finalCouponPolicy.getRemainingCount()).isEqualByComparingTo(0);
                    return null;
                });
            }
        );
    }
}
