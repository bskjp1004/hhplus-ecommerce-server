package kr.hhplus.be.server.product.application;

import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.infra.ProductRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepositoryImpl productRepository;

    public Product getProduct(long productId){
        return productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
