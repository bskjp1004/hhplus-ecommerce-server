package kr.hhplus.be.server.coupon.infra.kafka;

import kr.hhplus.be.server.coupon.application.dto.CouponQueueResponseDto;
import kr.hhplus.be.server.coupon.domain.event.CouponIssueRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponKafkaProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${spring.kafka.topics.coupon-issue}")
    private String couponIssueTopic;
    
    public CouponQueueResponseDto publishCouponIssueRequest(Long userId, Long couponPolicyId) {
        try {
            CouponIssueRequest request = CouponIssueRequest.create(userId, couponPolicyId);
            
            // couponPolicyId를 파티션 키로 사용하여 선착순 보장
            String partitionKey = String.valueOf(couponPolicyId);
            
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(couponIssueTopic, partitionKey, request);
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("쿠폰 발급 요청 메시지 전송 실패: userId={}, couponPolicyId={}", 
                        userId, couponPolicyId, ex);
                }
            });
            
            return CouponQueueResponseDto.pending(userId, couponPolicyId, null);
            
        } catch (Exception e) {
            log.error("쿠폰 발급 요청 메시지 발행 중 오류 발생: userId={}, couponPolicyId={}", userId, couponPolicyId, e);
            return CouponQueueResponseDto.failed(userId, couponPolicyId, "메시지 발행 실패");
        }
    }
}