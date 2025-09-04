package kr.hhplus.be.server.coupon.infra.kafka;

import kr.hhplus.be.server.config.error.BusinessException;
import kr.hhplus.be.server.config.error.ErrorCode;
import kr.hhplus.be.server.coupon.application.CouponService;
import kr.hhplus.be.server.coupon.domain.event.CouponIssueRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueEventListener {

    private final CouponService couponService;
    
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        exclude = {BusinessException.class}
    )
    @KafkaListener(
        topics = "${spring.kafka.topics.coupon-issue}",
        groupId = "coupon-issue-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleCouponIssueRequest(
            @Payload CouponIssueRequest request,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        try {
            couponService.issueLimitedCoupon(request.getUserId(), request.getCouponPolicyId());

            ack.acknowledge();
            
        } catch (BusinessException e) {
            // 비즈니스 예외는 재시도하지 않고 정상 완료 처리
            ack.acknowledge();
            
        } catch (Exception e) {
            log.error("쿠폰 발급 처리 중 시스템 오류: userId={}, couponPolicyId={}", 
                request.getUserId(), request.getCouponPolicyId(), e);
            throw e;
        }
    }
    
    @KafkaListener(
        topics = "${spring.kafka.topics.coupon-issue}.DLT",
        groupId = "coupon-issue-dlt-consumer-group",
        containerFactory = "dltListenerContainerFactory"
    )
    public void handleDLTMessage(
            @Payload CouponIssueRequest request,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        // DLT 메시지에 대한 추가 처리
        log.error("DLT 메시지 처리 - 최종 실패: userId={}, couponPolicyId={}, partition={}, offset={}", 
            request.getUserId(), request.getCouponPolicyId(), partition, offset);

        ack.acknowledge();
    }
}