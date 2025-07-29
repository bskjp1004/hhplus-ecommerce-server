package kr.hhplus.be.server.product.application;

import java.util.List;
import kr.hhplus.be.server.order.application.dto.OrderItemRequestDto;
import kr.hhplus.be.server.product.application.dto.ProductResponseDto;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.port.ProductRepository;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product getProductDomain(long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    public List<Product> getProductDomains(List<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds);

        return products;
    }

    public ProductResponseDto getProduct(long productId){
        return productRepository
            .findById(productId)
            .map(ProductResponseDto::from )
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

public interface ProductService {
    Product getProductDomain(long productId);
    List<Product> getProductDomains(List<Long> productIds);
    ProductResponseDto getProduct(long productId);
    Product decreaseProductStock(OrderItemRequestDto request);
    List<Product> decreaseProductStocks(List<OrderItemRequestDto> requests);
}
