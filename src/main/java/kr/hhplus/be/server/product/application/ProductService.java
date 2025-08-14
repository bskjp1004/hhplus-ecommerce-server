package kr.hhplus.be.server.product.application;

import java.math.BigDecimal;
import java.util.List;
import kr.hhplus.be.server.order.application.dto.OrderItemCommand;
import kr.hhplus.be.server.product.application.dto.ProductResponseDto;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.port.ProductRepository;
import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    @Transactional(propagation = Propagation.REQUIRED)
    public Product decreaseProductStock(OrderItemCommand command) {
        Product originalProduct = productRepository.findById(command.productId())
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Product updatedProduct = originalProduct.decreaseStock(command.quantity());

        Product persistedProduct = productRepository.insertOrUpdate(updatedProduct);

        return persistedProduct;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<Product> decreaseProductStocks(List<OrderItemCommand> commands) {
        return commands.stream()
            .sorted((a, b) -> Long.compare(a.productId(), b.productId()))
            .map(this::decreaseProductStock)
            .toList();
    }

    public BigDecimal calculateTotalAmount(List<OrderItemCommand> orderItemCommands, BigDecimal discountRate) {
        BigDecimal totalPrice = orderItemCommands.stream()
            .sorted((a, b) -> Long.compare(a.productId(), b.productId()))
            .map(orderItemCommand -> {
                Product product = productRepository.findById(orderItemCommand.productId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
                return product.getPrice().multiply(BigDecimal.valueOf(orderItemCommand.quantity()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalPrice.multiply(BigDecimal.ONE.subtract(discountRate));
    }
}
