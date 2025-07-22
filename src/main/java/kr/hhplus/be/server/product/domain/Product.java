package kr.hhplus.be.server.product.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class  Product {
    private final long id;
    private final BigDecimal price;
    private final long stock;


}
