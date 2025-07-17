package com.logmate.streaming.producer.log.producer;

import com.logmate.streaming.common.constant.kafka.KafkaConstant;
import com.logmate.streaming.common.dto.SpringBootParsedLog;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class KafkaLogProducer {

  private final KafkaTemplate<String, SpringBootParsedLog> kafkaTemplate;
  private final KafkaConstant kafkaConstant;

  public Mono<Void> sendLog(SpringBootParsedLog logMessage) {
    return Mono.fromRunnable(() ->
        kafkaTemplate.send(kafkaConstant.topic.LOG_TOPIC, logMessage)
    );
  }
}
