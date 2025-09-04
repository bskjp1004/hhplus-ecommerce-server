package kr.hhplus.be.server.coupon.integration;

import kr.hhplus.be.server.config.TestcontainersConfiguration;
import kr.hhplus.be.server.coupon.application.dto.CouponQueueResponseDto;
import kr.hhplus.be.server.coupon.controller.CouponController;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import kr.hhplus.be.server.coupon.domain.port.CouponPolicyRepository;
import kr.hhplus.be.server.coupon.domain.port.UserCouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
    partitions = 10,
    topics = {"coupon-issue-topic", "coupon-issue-topic.DLT"},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    }
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=localhost:9092",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "spring.kafka.consumer.group-id=test-coupon-group"
})
@DisplayName("Kafka 기반 쿠폰 테스트")
class KafkaCouponConcurrencyTest extends TestcontainersConfiguration {

    @Autowired
    private CouponController couponController;

    @Autowired
    private CouponPolicyRepository couponPolicyRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    private CouponPolicy limitedCouponPolicy;

    @BeforeEach
    void setUp() {
        // 제한된 수량의 쿠폰 정책 생성 (10개)
        limitedCouponPolicy = CouponPolicy.builder()
            .discountRate(BigDecimal.valueOf(0.1))
            .totalCount(10)
            .remainingCount(10)
            .build();
        
        limitedCouponPolicy = couponPolicyRepository.insertOrUpdate(limitedCouponPolicy);
    }

    @Test
    @DisplayName("동시에 100명이 요청해도 정확히 10개만 발급된다")
    void concurrentCouponIssue_FirstComeFirstServed() throws InterruptedException {
        // Given
        int totalUsers = 100;
        int expectedIssuedCount = 10;
        long couponPolicyId = limitedCouponPolicy.getId();
        
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(totalUsers);
        
        List<CouponQueueResponseDto> responses = Collections.synchronizedList(new ArrayList<>());

        // When - 100명의 사용자가 동시에 쿠폰 발급 요청
        IntStream.range(1, totalUsers + 1)
            .forEach(userId -> executorService.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 준비될 때까지 대기
                    
                    CouponQueueResponseDto response = couponController
                        .issueLimitedCoupon(userId, couponPolicyId)
                        .getBody();
                    
                    responses.add(response);
                    
                } catch (Exception e) {
                    System.err.println("Error for user " + userId + ": " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            }));

        startLatch.countDown();
        
        boolean completed = endLatch.await(10, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        
        executorService.shutdown();

        // Then - Kafka 메시지 처리 완료까지 대기 (최대 15초)
        await().atMost(java.time.Duration.ofSeconds(15))
            .untilAsserted(() -> {
                // 실제 발급된 쿠폰 수 확인
                List<UserCoupon> issuedCoupons = userCouponRepository.findAllByCouponPolicyId(couponPolicyId);

                // 중복 발급 확인 - 각 사용자는 최대 1개만 가져야 함
                List<Long> userIds = issuedCoupons.stream()
                    .map(UserCoupon::getUserId)
                    .distinct()
                    .toList();

                // 쿠폰 정책의 재고 확인
                CouponPolicy updatedPolicy = couponPolicyRepository.findById(couponPolicyId).orElseThrow();

                assertAll(
                    () -> assertThat(issuedCoupons).hasSize(expectedIssuedCount),
                    () -> assertThat(userIds).hasSize(expectedIssuedCount),
                    () -> assertThat(updatedPolicy.getRemainingCount()).isEqualTo(0)
                );
            });
    }

}