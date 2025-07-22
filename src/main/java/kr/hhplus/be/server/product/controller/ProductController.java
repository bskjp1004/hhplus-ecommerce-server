package kr.hhplus.be.server.product.controller;

import kr.hhplus.be.server.product.controller.dto.ProductResponse;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.application.ProductService;
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

    @GetMapping("{id}")
    public ResponseEntity<ProductResponse> getProduct(
        @PathVariable long id
    ) {
        Product product = pointService.getProduct(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }
}
