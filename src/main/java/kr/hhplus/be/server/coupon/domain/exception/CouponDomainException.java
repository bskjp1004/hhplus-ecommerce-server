package kr.hhplus.be.server.coupon.domain.exception;

public class CouponDomainException extends RuntimeException {
    private CouponDomainException(String message){ super(message); }

    public static class AlreadyUsedCouponException extends CouponDomainException{
        public AlreadyUsedCouponException(){
            super("이미 사용한 쿠폰입니다.");
        }
    }
}
