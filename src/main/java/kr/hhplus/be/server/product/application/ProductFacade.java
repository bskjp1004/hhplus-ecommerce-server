package kr.hhplus.be.server.product.application;

import java.util.List;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.order.application.OrderService;
import kr.hhplus.be.server.product.application.dto.ProductResult;
import kr.hhplus.be.server.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductFacade {
    
    private final ProductService productService;
    private final OrderService orderService;

    @Cacheable(
        value = "topSellingProducts",
        key = "'days:3:limit:5'",
        unless = "#result.isEmpty()"
    )
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