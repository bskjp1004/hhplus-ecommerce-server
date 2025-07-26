package kr.hhplus.be.server.order.domain.exception;

public class OrderDomainException extends RuntimeException{
    private OrderDomainException(String message){ super(message); }

    public static class EmptyOrderItemsException extends OrderDomainException{
        public EmptyOrderItemsException(){ super("주문 할 상품이 1개 이상이어야 합니다."); }
    }
}
