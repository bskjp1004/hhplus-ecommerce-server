package kr.hhplus.be.server.product.application;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import kr.hhplus.be.server.config.redis.RedisKey;
import kr.hhplus.be.server.config.redis.RedisZSetUtils;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.order.application.OrderService;
import kr.hhplus.be.server.product.application.dto.ProductResult;
import kr.hhplus.be.server.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductFacade {
    
    private final ProductService productService;
    private final OrderService orderService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Cacheable(
        value = "#{T(kr.hhplus.be.server.config.redis.RedisKey).CACHE_PRODUCT_TOP_SELLING_3_DAYS_TOP_5.key()}",
        key = "'days:3:limit:5'",
        unless = "#result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public List<ProductResult> getTopSellingProductsFromRedis() {
        List<Long> topSellingProductIds = getTopProductIdsFromRedis();

        if (topSellingProductIds.isEmpty()) {
            getTopSellingProducts();
        }

        List<Product> products = productService.getProductDomains(topSellingProductIds);

        return topSellingProductIds.stream()
            .map(productId -> products.stream()
                .filter(product -> product.getId() == productId)
                .findFirst()
                .map(ProductResult::from)
                .orElse(null))
            .filter(result -> result != null)
            .toList();
    }

    public List<Long> getTopProductIdsFromRedis() {
        int days = 3;
        int limit = 5;
        LocalDateTime endDate = LocalDateTime.now();
        LocalDate startDate = (endDate.toLocalDate()).minusDays(days - 1);

        String startStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String endStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        String sumKey = RedisKey.PRODUCT_RANK_SUM_RANGE.key(startStr, endStr);
        Duration ttl = RedisKey.PRODUCT_RANK_SUM_RANGE.ttlFromNow(endDate);

        if (!redisTemplate.hasKey(sumKey)) {
            // 3일치 일별 데이터 합산
            List<String> dailyKeys = new ArrayList<>();
            for (int i = 0; i < days; i++) {
                LocalDate date = (endDate.toLocalDate()).minusDays(i);
                String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                dailyKeys.add(RedisKey.PRODUCT_RANK_DAILY.key(dateStr));
            }

            // ZUNIONSTORE 실행
            redisTemplate.opsForZSet()
                .unionAndStore(null, dailyKeys, sumKey);

            // 10분 TTL 설정
            redisTemplate.expire(sumKey, ttl);
        }

        // 상위 5개 조회
        Set<TypedTuple<Object>> topProducts =
            redisTemplate.opsForZSet()
                .reverseRangeWithScores(sumKey, 0, limit - 1);

        return RedisZSetUtils.convertToLongList(topProducts);
    }

    @Transactional(readOnly = true)
    public List<ProductResult> getTopSellingProducts() {
        // OrderService를 통해 최근 3일간 가장 많이 팔린 상품ID 5개 조회
        List<Long> topSellingProductIds = orderService.getTopSellingProductIds(3, 5);

        if (topSellingProductIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SALES_DATA);
        }

        List<Product> products = productService.getProductDomains(topSellingProductIds);

        return topSellingProductIds.stream()
            .map(productId -> products.stream()
                .filter(product -> product.getId() == productId)
                .findFirst()
                .map(ProductResult::from)
                .orElse(null))
            .filter(result -> result != null)
            .toList();
    }
}