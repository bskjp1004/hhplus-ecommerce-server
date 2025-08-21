package kr.hhplus.be.server.config.redis.lock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.RedissonMultiLock;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;

    @Around("@annotation(kr.hhplus.be.server.config.redis.lock.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        List<String> lockKeys = new ArrayList<>();
        String lockTypePrefix = distributedLock.lockType().getPrefix();
        
        for (String keyExpression : distributedLock.keys()) {
            Object keyValue = CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(), joinPoint.getArgs(), keyExpression
            );
            
            // 리스트인 경우 각 요소를 개별 락 키로 생성
            if (keyValue instanceof List<?>) {
                for (Object item : (List<?>) keyValue) {
                    String lockKey = REDISSON_LOCK_PREFIX + lockTypePrefix + ":" + item;
                    lockKeys.add(lockKey);
                }
            } else {
                String lockKey = REDISSON_LOCK_PREFIX + lockTypePrefix + ":" + keyValue;
                lockKeys.add(lockKey);
            }
        }
        
        // 락 키 정렬
        Collections.sort(lockKeys);
        
        List<RLock> rLocks = new ArrayList<>();
        for (String lockKey : lockKeys) {
            rLocks.add(redissonClient.getLock(lockKey));
            log.debug("Lock key created: {}", lockKey);
        }

        // 멀티락 생성
        RLock multiLock = new RedissonMultiLock(rLocks.toArray(new RLock[0]));

        try {
            boolean available = multiLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                throw new IllegalStateException("멀티락 획득 실패");
            }

            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("멀티락 획득 중 인터럽트 발생", e);
        } finally {
            if (multiLock.isHeldByCurrentThread()) {
                multiLock.unlock();
            }
        }
    }
}
