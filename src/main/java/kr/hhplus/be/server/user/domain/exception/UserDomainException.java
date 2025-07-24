package kr.hhplus.be.server.user.domain.exception;

public class UserDomainException extends RuntimeException {
    private UserDomainException(String message){
        super(message);
    }

    public static class IllegalAmountException extends UserDomainException{
        public IllegalAmountException(){ super("요청 금액은 1이상 이어야 합니다."); }
    }

    public static class ExceedMaxBalanceException extends UserDomainException{
        public ExceedMaxBalanceException(){
            super("잔액 충전은 최대 잔고를 넘을 수 없습니다.");
        }
    }

    public static class InsufficientBalanceException extends UserDomainException{
        public InsufficientBalanceException(){
            super("잔액이 부족합니다.");
        }
    }
}
