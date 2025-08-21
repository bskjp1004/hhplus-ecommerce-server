package kr.hhplus.be.server.product.application;

import kr.hhplus.be.server.config.redis.RedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCacheScheduler {

    private final CacheManager cacheManager;
    private final ProductFacade productFacade;

    @Scheduled(cron = "0 */10 * * * *")
    public void refreshTopSellingProducts() {
        try {
            String cacheKey = RedisKey.CACHE_PRODUCT_TOP_SELLING_3_DAYS_TOP_5.key();
            Cache cache = cacheManager.getCache(cacheKey);
            if (cache != null) {
                cache.clear();
            }

            productFacade.getTopSellingProductsFromRedis();
            log.info("인기 상품 캐시 갱신 완료");
        } catch (Exception e) {
            log.error("인기 상품 캐시 갱신 실패", e);
        }
    }
}
