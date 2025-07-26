package kr.hhplus.be.server.order.infra.entity;

import jakarta.persistence.Entity;
import java.math.BigDecimal;
import kr.hhplus.be.server.order.domain.OrderItem;
import lombok.AllArgsConstructor;

@Entity
@AllArgsConstructor
public class OrderItemEntity {
    private final long id;
    private final long orderId;
    private final long productId;
    private final Integer quantity;
    private final BigDecimal productPrice;

    public OrderItem toDomain(){
        return OrderItem.builder()
            .id(this.id)
            .orderId(this.orderId)
            .productId(this.productId)
            .quantity(this.quantity)
            .productPrice(this.productPrice)
            .build();
    }

    public static OrderItemEntity fromDomain(OrderItem orderItem){
        return new OrderItemEntity(
            orderItem.getId(),
            orderItem.getOrderId(),
            orderItem.getProductId(),
            orderItem.getQuantity(),
            orderItem.getProductPrice()
        );
    }
}
