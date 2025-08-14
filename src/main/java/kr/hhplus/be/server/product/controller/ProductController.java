package kr.hhplus.be.server.product.controller;

import kr.hhplus.be.server.product.application.ProductService;
import kr.hhplus.be.server.product.application.dto.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService pointService;

    @GetMapping("{productId}")
    public ResponseEntity<ProductResponseDto> getProduct(
        @PathVariable long productId
    ) {
        return ResponseEntity.ok(pointService.getProduct(productId));
    }
}
