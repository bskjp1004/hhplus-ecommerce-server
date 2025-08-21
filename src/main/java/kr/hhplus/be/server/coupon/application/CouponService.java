package kr.hhplus.be.server.coupon.application;

import java.time.LocalDateTime;
import java.util.Map;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.config.redis.RedisKey;
import kr.hhplus.be.server.coupon.application.dto.UserCouponResponseDto;
import kr.hhplus.be.server.coupon.application.dto.CouponQueueResponseDto;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import kr.hhplus.be.server.coupon.domain.exception.CouponDomainException;
import kr.hhplus.be.server.coupon.domain.port.CouponPolicyRepository;
import kr.hhplus.be.server.coupon.domain.port.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponRepository userCouponRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TaskExecutor asyncExecutor;

    private static final String TOTAL_STOCK_HASH_KEY = "total_count";
    private static final String REMAIN_STOCK_HASH_KEY = "remaining_count";

    public CouponPolicy getCouponPolicyDomain(long couponPolicyId) {
        return couponPolicyRepository.findById(couponPolicyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_POLICY_NOT_FOUND));
    }

    public CouponPolicy getCouponPolicyDomainWithLock(long couponPolicyId) {
        return couponPolicyRepository.findByIdWithLock(couponPolicyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_POLICY_NOT_FOUND));
    }

    public UserCoupon getCouponDomain(long userCouponId) {
        return userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
    }

    public UserCouponResponseDto getCoupon(long userCouponId) {
        return UserCouponResponseDto.from(getCouponDomain(userCouponId));
    }

    @Transactional
    public UserCouponResponseDto useCoupon(long userCouponId) {
        UserCoupon issuedUserCoupon = userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        UserCoupon useCoupon = issuedUserCoupon.useCoupon();

        UserCoupon persistedUserCoupon = userCouponRepository.insertOrUpdate(useCoupon);

        return UserCouponResponseDto.from(persistedUserCoupon);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public BigDecimal applyCouponForOrder(long userCouponId) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        try {
            UserCoupon usedCoupon = userCoupon.useCoupon();
            userCouponRepository.insertOrUpdate(usedCoupon);
        } catch (OptimisticLockingFailureException e) {
            throw new CouponDomainException.AlreadyUsedCouponException();
        }

        CouponPolicy couponPolicy = getCouponPolicyDomainWithLock(userCoupon.getCouponPolicyId());
        return couponPolicy.getDiscountRate();
    }

    @Transactional
    public UserCouponResponseDto issueLimitedCoupon(long userId, long couponPolicyId){
        CouponPolicy couponPolicy = couponPolicyRepository.findByIdWithLock(couponPolicyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_POLICY_NOT_FOUND));

        if (!couponPolicy.canIssue())
        {
            throw new BusinessException(ErrorCode.COUPON_OUT_OF_STOCK);
        }

        couponPolicyRepository.insertOrUpdate(couponPolicy.issue());

        UserCoupon userCoupon = UserCoupon.create(couponPolicy.getId(), userId);

        UserCoupon persistedUserCoupon = userCouponRepository.insertOrUpdate(userCoupon);

        return UserCouponResponseDto.from(persistedUserCoupon);
    }

    public CouponQueueResponseDto issueLimitedCouponFromRedis(long userId, long couponPolicyId) {
        String key_requestQueue = RedisKey.COUPON_QUEUE.key(couponPolicyId);
        Duration ttl_requestQueue = RedisKey.COUPON_QUEUE.ttlFromNow();

        // 1. Redis Hash에서 쿠폰 재고 정보 조회
        Map<String, Long> couponStock = getCouponStockFromRedis(couponPolicyId);
        long couponTotalCount = couponStock.get(TOTAL_STOCK_HASH_KEY);
        long couponRemainingCount = couponStock.get(REMAIN_STOCK_HASH_KEY);

        // 2. Redis Hash에서 쿠폰 남은 재고 없으면 반환
        if (couponRemainingCount <= 0){
            return CouponQueueResponseDto.soldOut(userId, couponPolicyId,
                String.valueOf(ErrorCode.COUPON_OUT_OF_STOCK));
        }

        // 3. 쿠폰 발급 큐에 ZADD NX로 추가 (이미 존재하면 추가하지 않음)
        double score = System.nanoTime();
        Boolean added = redisTemplate.opsForZSet().addIfAbsent(key_requestQueue, userId, score);

        if (added == null || !added) {
            return CouponQueueResponseDto.failed(userId, couponPolicyId,
                String.valueOf(ErrorCode.COUPON_ALREADY_REQUEST));
        }

        // 쿠폰 발급 큐 첫 번째 사용자일 경우만 TTL 설정
        Long queueSize = redisTemplate.opsForZSet().size(key_requestQueue);
        if (queueSize != null && queueSize == 1) {
            redisTemplate.expire(key_requestQueue, ttl_requestQueue);
        }

        // 4. 쿠폰 발급 큐에 추가 후 순위 확인
        Long rank = redisTemplate.opsForZSet().rank(key_requestQueue, userId);
        
        if (rank == null) {
            return CouponQueueResponseDto.failed(userId, couponPolicyId,
                String.valueOf(ErrorCode.COUPON_ISSUED_UNKNOWN_ERROR));
        }
        
        // 5. 순위가 쿠폰 발행 수량 초과인지 확인
        if (rank >= couponTotalCount) {
            return CouponQueueResponseDto.soldOut(userId, couponPolicyId,
                String.valueOf(ErrorCode.COUPON_OUT_OF_STOCK));
        }
        
        // 6. 비동기 DB 처리 (한도 내 사용자만)
        asyncExecutor.execute(() -> processActualCouponIssuance(userId, couponPolicyId));
        
        return CouponQueueResponseDto.pending(userId, couponPolicyId, rank + 1);
    }

    private Map<String, Long> getCouponStockFromRedis(long couponPolicyId) {
        String key_couponStock = RedisKey.COUPON_POLICY_STOCK.key(couponPolicyId);

        // Redis Hash에서 totalCount, remainingCount 조회
        Object totalCount = redisTemplate.opsForHash().get(key_couponStock, TOTAL_STOCK_HASH_KEY);
        Object remainingCount = redisTemplate.opsForHash().get(key_couponStock, REMAIN_STOCK_HASH_KEY);

        if (totalCount != null && remainingCount != null) {
            return Map.of(
                TOTAL_STOCK_HASH_KEY, Long.parseLong(totalCount.toString()),
                REMAIN_STOCK_HASH_KEY, Long.parseLong(remainingCount.toString())
            );
        }

        // Redis에 없으면 DB에서 조회 후 Redis에 저장
        return initializeCouponStockInRedis(couponPolicyId);
    }

    private Map<String, Long> initializeCouponStockInRedis(long couponPolicyId) {
        CouponPolicy couponPolicy = getCouponPolicyDomain(couponPolicyId);

        String key_couponStock = RedisKey.COUPON_POLICY_STOCK.key(couponPolicyId);
        Duration ttl_couponStock = RedisKey.COUPON_POLICY_STOCK.ttlFromNow();

        // 키가 존재하지 않을 때만 초기화 및 TTL 설정
        if (!redisTemplate.hasKey(key_couponStock)) {
            // Hash에 totalCount, remainingCount 저장
            redisTemplate.opsForHash().put(key_couponStock, TOTAL_STOCK_HASH_KEY, String.valueOf(couponPolicy.getTotalCount()));
            redisTemplate.opsForHash().put(key_couponStock, REMAIN_STOCK_HASH_KEY, String.valueOf(couponPolicy.getRemainingCount()));
            redisTemplate.expire(key_couponStock, ttl_couponStock);
        }

        return Map.of(
            TOTAL_STOCK_HASH_KEY, (long) couponPolicy.getTotalCount(),
            REMAIN_STOCK_HASH_KEY, (long) couponPolicy.getRemainingCount()
        );
    }

    @Async
    @Transactional
    public void processActualCouponIssuance(Long userId, Long couponPolicyId) {
        try {
            // DB에서 쿠폰 정책 조회
            CouponPolicy couponPolicy = couponPolicyRepository.findById(couponPolicyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_POLICY_NOT_FOUND));

            // 쿠폰 재고 확인
            if (!couponPolicy.canIssue()) {
                updateCouponStatus(CouponQueueResponseDto.soldOut(userId, couponPolicyId,
                    String.valueOf(ErrorCode.COUPON_OUT_OF_STOCK))
                );
                return;
            }

            // 쿠폰 발급 처리
            CouponPolicy updatedCouponPolicy = couponPolicyRepository.insertOrUpdate(couponPolicy.issue());
            UserCoupon userCoupon = UserCoupon.create(couponPolicy.getId(), userId);
            UserCoupon persistedUserCoupon = userCouponRepository.insertOrUpdate(userCoupon);

            // Redis 쿠폰 정책 남은 재고수량 업데이트
            String key_couponStock = RedisKey.COUPON_POLICY_STOCK.key(couponPolicyId);
            redisTemplate.opsForHash().put(key_couponStock, REMAIN_STOCK_HASH_KEY, String.valueOf(updatedCouponPolicy.getRemainingCount()));

            // 발급 완료 상태로 업데이트
            updateCouponStatus(
                CouponQueueResponseDto.issued(persistedUserCoupon.getUserId(), persistedUserCoupon.getCouponPolicyId())
            );

        } catch (Exception e) {
            // 실패 상태로 업데이트
            updateCouponStatus(CouponQueueResponseDto.failed(userId, couponPolicyId,
                String.valueOf(ErrorCode.COUPON_ISSUED_UNKNOWN_ERROR))
            );
            log.error("쿠폰 발급 실패: userId={}, couponPolicyId={}", userId, couponPolicyId, e);
        }
    }

    private void updateCouponStatus(CouponQueueResponseDto result) {
        try {
            String statusKey = RedisKey.COUPON_STATUS.key(result.getCouponPolicyId(), result.getUserId());
            Duration ttl = RedisKey.COUPON_STATUS.ttlFromNow(LocalDateTime.now());

            redisTemplate.opsForValue().set(statusKey, result, ttl);
        } catch (Exception e) {
            log.error("쿠폰 상태 업데이트 실패: userId={}, couponPolicyId={}", result.getUserId(), result.getCouponPolicyId(), e);
        }
    }
}
