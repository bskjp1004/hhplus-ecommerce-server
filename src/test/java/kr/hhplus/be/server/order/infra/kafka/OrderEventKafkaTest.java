package kr.hhplus.be.server.order.infra.kafka;

import kr.hhplus.be.server.config.TestcontainersConfiguration;
import kr.hhplus.be.server.order.domain.event.OrderPlacedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Testcontainers
@SpringBootTest
@DisplayName("주문 정보 외부 플랫폼 전송 테스트(Kafka)")
class OrderEventKafkaTest extends TestcontainersConfiguration {

    @Autowired
    private OrderEventProducer orderEventProducer;

    private KafkaConsumer<String, OrderPlacedEvent> consumer;

    @BeforeEach
    void setUp() {
        consumer = new KafkaConsumer<>(createKafkaConsumerProperties());
        consumer.subscribe(Collections.singletonList("order-placed-events"));
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    @DisplayName("OrderEventProducer를 통해 Kafka에 메시지가 전송되고 Consumer가 수신한다")
    void testKafkaMessageSendAndReceive() {
        // Given
        OrderPlacedEvent event = OrderPlacedEvent.of(
            123L,
            456L,
            BigDecimal.valueOf(100000),
            List.of(
                new OrderPlacedEvent.Item(1L, 2, BigDecimal.valueOf(30000)),
                new OrderPlacedEvent.Item(2L, 1, BigDecimal.valueOf(40000))
            )
        );

        // When
        orderEventProducer.sendOrderPlacedEvent(event);

        // Then
        ConsumerRecords<String, OrderPlacedEvent> records = consumer.poll(Duration.ofSeconds(10));
        ConsumerRecord<String, OrderPlacedEvent> record = records.iterator().next();
        assertAll(
            () -> assertThat(record.value()).isNotNull(),
            () -> assertThat(record.value().orderId()).isEqualTo(123L)
        );
    }
}