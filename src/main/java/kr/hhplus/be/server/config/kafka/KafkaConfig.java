package kr.hhplus.be.server.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 설정 클래스(application.yml 기반)
 */
@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    
    private final KafkaProperties kafkaProperties;
    
    @Value("${spring.kafka.topics.order-placed}")
    private String orderPlacedTopic;
    
    @Value("${spring.kafka.topics.coupon-issue}")
    private String couponIssueTopic;

    /**
     * 주문 이벤트 토픽
     */
    @Bean
    public NewTopic orderPlacedTopic() {
        return TopicBuilder.name(orderPlacedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    /**
     * 쿠폰 발급 요청 토픽
     */
    @Bean
    public NewTopic couponIssueTopic() {
        return TopicBuilder.name(couponIssueTopic)
            .partitions(10)
            .replicas(1)                          // EmbeddedKafka 호환 (단일 브로커)
            .config("retention.ms", "604800000")  // 7일
            //.config("min.insync.replicas", "1")   // EmbeddedKafka 호환
            .config("cleanup.policy", "delete")   // 정리 정책
            .build();
    }

    /**
     * Producer Factory
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        return new DefaultKafkaProducerFactory<>(props);
    }
    
    /**
     * 기본 KafkaTemplate
     */
    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    /**
     * RetryableTopic용 KafkaTemplate
     */
    @Bean(name = "defaultRetryTopicKafkaTemplate")
    public KafkaTemplate<String, Object> defaultRetryTopicKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Consumer Factory
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            "org.springframework.kafka.support.serializer.ErrorHandlingDeserializer");
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    /**
     * Kafka Listener Container Factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        factory.setConcurrency(kafkaProperties.getListener().getConcurrency());
        
        factory.getContainerProperties().setPollTimeout(
            kafkaProperties.getListener().getPollTimeout().toMillis()
        );
        
        return factory;
    }
    
    /**
     * DLT용 Kafka Listener Container Factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> dltListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        factory.setConcurrency(1);
        
        return factory;
    }
}