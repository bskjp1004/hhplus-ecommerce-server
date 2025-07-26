package kr.hhplus.be.server.product.application;

import java.util.List;
import kr.hhplus.be.server.order.application.dto.OrderItemRequestDto;
import kr.hhplus.be.server.product.application.dto.ProductResponseDto;
import kr.hhplus.be.server.product.domain.Product;

public interface ProductService {
    Product getProductDomain(long productId);
    List<Product> getProductDomains(List<Long> productIds);
    ProductResponseDto getProduct(long productId);
    Product decreaseProductStock(OrderItemRequestDto request);
    List<Product> decreaseProductStocks(List<OrderItemRequestDto> requests);
}
