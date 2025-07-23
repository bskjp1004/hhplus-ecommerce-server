package kr.hhplus.be.server.product.application;

import kr.hhplus.be.server.product.application.dto.ProductResponseDto;

public interface ProductService {
    ProductResponseDto getProduct(long productId);
}
