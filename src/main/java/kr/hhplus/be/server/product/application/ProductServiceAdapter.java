package kr.hhplus.be.server.product.application;

import kr.hhplus.be.server.product.application.dto.ProductResponseDto;
import kr.hhplus.be.server.product.domain.ProductRepository;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceAdapter implements ProductService {

    private final ProductRepository productRepository;

    public ProductResponseDto getProduct(long productId){
        return productRepository
            .findById(productId)
            .map(ProductResponseDto::from )
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
