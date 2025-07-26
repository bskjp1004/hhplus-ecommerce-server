package kr.hhplus.be.server.product.domain.exception;

import kr.hhplus.be.server.user.domain.exception.UserDomainException;

public class ProductDomainException extends RuntimeException {
    private ProductDomainException(String message){
        super(message);
    }

    public static class IllegalStockException extends ProductDomainException{
        public IllegalStockException(){ super("요청 재고 수량은 1이상 이어야 합니다."); }
    }

    public static class ExceedMaxStockException extends ProductDomainException {
        public ExceedMaxStockException(){
            super("재고 수량은 최대 재고 수량을 넘을 수 없습니다.");
        }
    }

    public static class InsufficientStockException extends ProductDomainException{
        public InsufficientStockException(){
            super("재고 수량이 부족합니다.");
        }
    }
}
