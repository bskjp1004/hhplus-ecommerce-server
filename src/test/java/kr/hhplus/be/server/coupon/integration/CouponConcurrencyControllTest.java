package kr.hhplus.be.server.coupon.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import kr.hhplus.be.server.BaseConcurrencyTest;
import kr.hhplus.be.server.config.redis.RedisKey;
import kr.hhplus.be.server.coupon.application.CouponService;
import kr.hhplus.be.server.coupon.application.dto.CouponQueueResponseDto;
import kr.hhplus.be.server.coupon.application.dto.CouponQueueResponseDto.CouponQueueStatus;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import kr.hhplus.be.server.coupon.domain.port.CouponPolicyRepository;
import kr.hhplus.be.server.coupon.domain.port.UserCouponRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.domain.port.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@DisplayName("쿠폰 동시성 제어 테스트")
public class CouponConcurrencyControllTest extends BaseConcurrencyTest {
    
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final int CONCURRENT_USERS = 1000;
    private static final int COUPON_LIMIT = 500;
    private CouponPolicy testCouponPolicy;
    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        // Redis 캐시 초기화
        clearRedisCache();
    }

    @Test
    @DisplayName("Redis 기반 선착순 쿠폰 발급 동시성 테스트(1000명이 동시에 500개 한정 쿠폰 발급)")
    void Redis_기반_선착순_쿠폰_발급_동시성_테스트() throws InterruptedException, ExecutionException {
        // given
        // 테스트 데이터 초기화
        initializeTestData();

        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_USERS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(CONCURRENT_USERS);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger soldOutCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<Future<CouponQueueResponseDto>> futures = new ArrayList<>();

        // when: 1000명 동시 요청
        long startTime = System.currentTimeMillis();

        for (User user : testUsers) {
            Future<CouponQueueResponseDto> future = executorService.submit(() -> {
                try {
                    startLatch.await();

                    CouponQueueResponseDto result = couponService.issueLimitedCouponFromRedis(
                        user.getId(),
                        testCouponPolicy.getId()
                    );

                    // 결과 집계
                    switch (result.getStatus()) {
                        case PENDING:
                            successCount.incrementAndGet();
                            break;
                        case SOLD_OUT:
                            soldOutCount.incrementAndGet();
                            break;
                        case FAILED:
                            failCount.incrementAndGet();
                            break;
                    }

                    return result;
                } catch (Exception e) {
                    log.error("쿠폰 발급 요청 실패 - userId: {}", user.getId(), e);
                    failCount.incrementAndGet();
                    return null;
                } finally {
                    endLatch.countDown();
                }
            });
            futures.add(future);
        }

        // 모든 스레드 동시 시작
        startLatch.countDown();
        
        // 모든 요청 완료 대기
        boolean completed = endLatch.await(30, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // then
        assertAll(
            () -> assertThat(completed).as("모든 요청이 완료됨").isTrue(),
            () -> assertThat(successCount.get()).as("성공 건수는 쿠폰 한도와 일치해야 함")
                .isEqualTo(COUPON_LIMIT),
            () -> assertThat(soldOutCount.get()).as("품절 건수는 (전체 유저 - 쿠폰 한도)와 일치해야 함")
                .isEqualTo(CONCURRENT_USERS - COUPON_LIMIT)
        );

        log.info("========== 성능 테스트 결과 ==========");
        log.info("총 실행 시간: {} ms", duration);
        log.info("평균 처리 시간: {} ms/request", duration / (double) CONCURRENT_USERS);
        log.info("초당 처리량: {} requests/sec", CONCURRENT_USERS * 1000.0 / duration);
        log.info("=====================================");
        log.info("성공 (PENDING): {} 명", successCount.get());
        log.info("품절 (SOLD_OUT): {} 명", soldOutCount.get());
        log.info("실패: {} 명", failCount.get());
        log.info("=====================================");

        // 비동기 처리 대기 (실제 DB 발급 처리)
        Thread.sleep(5000);

        // 성공한 사용자들의 상태 확인
        int issuedCount = 0;
        for (Future<CouponQueueResponseDto> future : futures) {
            CouponQueueResponseDto result = future.get();
            if (result != null && CouponQueueStatus.PENDING.equals(result.getStatus())) {
                String statusKey = RedisKey.COUPON_STATUS.key(
                    result.getCouponPolicyId(),
                    result.getUserId()
                );
                CouponQueueResponseDto status = (CouponQueueResponseDto) redisTemplate.opsForValue().get(statusKey);
                if (status != null && CouponQueueStatus.ISSUED.equals(status.getStatus())) {
                    issuedCount++;
                }
            }
        }

        log.info("실제 발급 완료: {} 명", issuedCount);

        executorService.shutdown();
    }

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
                CouponPolicy finalCouponPolicy = couponPolicyRepository.findByIdWithLock(couponPolicy.getId()).orElseThrow();
                assertThat(finalCouponPolicy.getRemainingCount()).isEqualByComparingTo(0);
                    return null;
                });
            }
        );
    }

    private void clearRedisCache() {
        log.info("Redis 캐시 초기화 시작");

        // 쿠폰 관련 모든 키 패턴 삭제
        Set<String> couponQueueKeys = redisTemplate.keys("*coupon:request:queue*");
        Set<String> couponStatusKeys = redisTemplate.keys("*coupon:status*");
        Set<String> couponStockKeys = redisTemplate.keys("*coupon:policy:stock*");
        Set<String> couponCacheKeys = redisTemplate.keys("*cache:coupon*");

        if (!couponQueueKeys.isEmpty()) {
            redisTemplate.delete(couponQueueKeys);
        }

        if (!couponStatusKeys.isEmpty()) {
            redisTemplate.delete(couponStatusKeys);
        }

        if (!couponStockKeys.isEmpty()) {
            redisTemplate.delete(couponStockKeys);
        }

        if (!couponCacheKeys.isEmpty()) {
            redisTemplate.delete(couponCacheKeys);
        }

        log.info("Redis 캐시 초기화 완료");
    }

    private void initializeTestData() {
        // 쿠폰 정책 생성 (500개 한정)
        testCouponPolicy = CouponPolicy.builder()
            .discountRate(new BigDecimal("10.00"))
            .totalCount(COUPON_LIMIT)
            .remainingCount(COUPON_LIMIT)
            .build();
        testCouponPolicy = couponPolicyRepository.insertOrUpdate(testCouponPolicy);
        log.info("쿠폰 정책 생성 완료 - ID: {}, 한도: {}", testCouponPolicy.getId(), COUPON_LIMIT);

        // 테스트 유저 생성 (1000명)
        testUsers = new ArrayList<>();
        for (int i = 1; i <= CONCURRENT_USERS; i++) {
            User user = User.builder()
                .balance(new BigDecimal("100000"))
                .build();
            User savedUser = userRepository.insertOrUpdate(user);
            testUsers.add(savedUser);
        }
        log.info("테스트 유저 {} 명 생성 완료", CONCURRENT_USERS);
    }
}
