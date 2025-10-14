package com.logmate.streaming.messaging.producer.impl.kafka;

import com.logmate.streaming.global.constant.kafka.KafkaConstant;
import com.logmate.streaming.messaging.producer.DlqProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka DLQ(Dead Letter Queue) Producer
 *
 * Kafka 전송 실패 또는 처리 불가능한 로그를 DLQ 토픽으로 전달하는 책임을 가진 프로듀서.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaDlqProducer implements DlqProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final KafkaConstant kafkaConstant;

  @Override
  public void send(String key, String message, Throwable cause) {
    log.warn("[KafkaDlqProducer] Sending message to DLQ. topic={}, key={}, cause={}",
        kafkaConstant.topic.DLQ_TOPIC,
        key != null ? key : "null",
        cause != null ? cause.getMessage() : "unknown");

    kafkaTemplate.send(kafkaConstant.topic.DLQ_TOPIC, key, message)
        .whenComplete((result, ex) -> {
          if (ex == null) {
            log.info(
                "[KafkaDlqProducer] Message sent successfully. topic={}, partition={}, offset={}",
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
          } else {
            log.error("[KafkaDlqProducer] Failed to send message to DLQ. topic={}, key={}, error={}",
                kafkaConstant.topic.DLQ_TOPIC,
                key != null ? key : "null",
                ex.getMessage(),
                ex);
          }
        });
  }
}
