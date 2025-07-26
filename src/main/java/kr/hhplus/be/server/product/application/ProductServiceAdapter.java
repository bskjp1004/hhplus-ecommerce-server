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

@Service
@RequiredArgsConstructor
public class ProductServiceAdapter implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product getProductDomain(long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
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

    @Override
    public Product decreaseProductStock(OrderItemRequestDto requests) {
        Product originalProduct = productRepository.findById(requests.productId())
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Product updatedProduct = originalProduct.decreaseStock(requests.quantity());

        Product persistedProduct = productRepository.insertOrUpdate(updatedProduct);

        return persistedProduct;
    }

    @Override
    public List<Product> decreaseProductStocks(List<OrderItemRequestDto> requests) {
        return requests.stream()
            .map(this::decreaseProductStock)
            .toList();
    }
}
