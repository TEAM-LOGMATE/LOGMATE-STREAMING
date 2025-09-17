package com.logmate.streaming.producer.impl.kafka;

import com.logmate.streaming.common.constant.kafka.KafkaConstant;
import com.logmate.streaming.producer.DlqProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaDlqProducer implements DlqProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final KafkaConstant kafkaConstant;

  @Override
  public void send(String key, String message, Throwable cause) {
    log.warn("[KafkaDlqProducer] Sending message to DLQ. key={}, cause={}",
        key != null ? key : "null", cause != null ? cause.getMessage() : "unknown");

    kafkaTemplate.send(kafkaConstant.topic.DLQ_TOPIC, key, message)
        .whenComplete((result, ex) -> {
          if (ex == null) {
            log.info(
                "[KafkaDlqProducer] Message sent successfully. topic={}, partition={}, offset={}",
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
          } else {
            log.error("[KafkaDlqProducer] Failed to send message to DLQ", ex);
          }
        });
  }
}
