package kr.hhplus.be.server.config.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    PRODUCT_NOT_FOUND("상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COUPON_POLICY_NOT_FOUND("쿠폰정책을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COUPON_NOT_FOUND("쿠폰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    COUPON_OUT_OF_STOCK("발급 가능한 쿠폰이 없습니다.", HttpStatus.CONFLICT),
    COUPON_ALREADY_ISSUED("이미 발급받은 쿠폰입니다.", HttpStatus.CONFLICT),
    COUPON_ALREADY_REQUEST("중복된 쿠폰 발급 요청입니다.", HttpStatus.NOT_FOUND),
    COUPON_ISSUED_UNKNOWN_ERROR("알수없는 에러입니다.", HttpStatus.NOT_FOUND),

    INSUFFICIENT_SALES_DATA("인기 상품을 표시하기에 충분한 판매 데이터가 없습니다.", HttpStatus.NO_CONTENT);

    private final String message;
    private final HttpStatus status;
}
