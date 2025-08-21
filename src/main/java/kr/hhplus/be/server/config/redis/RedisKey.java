package kr.hhplus.be.server.config.redis;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RedisKey {
    // 캐시
    CACHE_PRODUCT_TOP_SELLING_3_DAYS_TOP_5("cache:product:top_selling", TtlPolicy.FIXED_MINUTES_10),

    // 당일 상품 실시간 랭킹 (ZSET) - TTL: 생성일 기준 D+3 00:10 만료
    PRODUCT_RANK_DAILY("zs:product:rank:%s", TtlPolicy.DAY_PLUS_3_AT_0010),

    // 합산 상품 랭킹 (ZSET) - TTL: 10분
    PRODUCT_RANK_SUM_RANGE("zs:product:rank:sum:%s_%s", TtlPolicy.FIXED_MINUTES_10),

    // 쿠폰 발급 큐 (ZSET) - TTL: 3분
    COUPON_QUEUE("zs:coupon:request:queue:%d", TtlPolicy.FIXED_MINUTES_3),

    // 쿠폰 발급 상태 비동기 응답 (SET) - TTL: 3분
    COUPON_STATUS("s:coupon:status:%d:%d", TtlPolicy.FIXED_MINUTES_3),

    // 쿠폰 정책 재고수량 (Hashes: total_count, remaining_count) - TTL: 생성일 기준 D+3 00:10 만료
    COUPON_POLICY_STOCK("h:coupon:policy:stock:%d", TtlPolicy.DAY_PLUS_3_AT_0010);

    private final String template;
    private final TtlPolicy ttlPolicy;

    public String key(Object... args) {
        return KeySpaces.withEnvAndTenant(template.formatted(args));
    }

    public Duration ttlFromNow(LocalDateTime baseDateTime) {
        return ttlPolicy.ttlFromNow(baseDateTime);
    }
    
    public Duration ttlFromNow() {
        return ttlPolicy.ttlFromNow(LocalDateTime.now());
    }
}

final class KeySpaces {
    static String withEnvAndTenant(String body) {
        String env = System.getProperty("spring.profiles.active", "local");
        return "%s:%s".formatted(env, body);

    }
}

enum TtlPolicy {
    FIXED_MINUTES_3 {
        @Override public Duration ttlFromNow(LocalDateTime ignore) {
            return Duration.ofMinutes(3);
        }
    },
    FIXED_MINUTES_10 {
        @Override public Duration ttlFromNow(LocalDateTime ignore) {
            return Duration.ofMinutes(10);
        }
    },
    FIXED_HOURS_3 {
        @Override public Duration ttlFromNow(LocalDateTime ignore) {
            return Duration.ofHours(3);
        }
    },
    DAY_PLUS_3_AT_0010 {
        @Override public Duration ttlFromNow(LocalDateTime baseDateTime) {
            var expireAt = (baseDateTime.toLocalDate()).plusDays(3).atTime(0, 10)
                .atZone(ZoneId.systemDefault()).toInstant();
            return Duration.between(Instant.now(), expireAt);
        }
    };
    public abstract Duration ttlFromNow(LocalDateTime baseDateTime);
}
