package kr.hhplus.be.server.product.infra;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ProductInMemoryRepository implements ProductRepository {

    // region
    /**
     * TODO - 실제 DB 연결 후 삭제하기
     * */
    private final Map<Long, Product> storage = new HashMap<>();
    public ProductInMemoryRepository() {
        storage.put(1L, Product.builder().id(1L).price(new BigDecimal("1000")).stock(10L).build());
        storage.put(2L, Product.builder().id(2L).price(new BigDecimal("1500")).stock(5L).build());
    }
    // endregion

    @Override
    public Optional<Product> findById(long productId) {
        return Optional.of(storage.get(productId));
    }
}
