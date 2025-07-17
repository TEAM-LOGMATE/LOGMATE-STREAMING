package com.logmate.streaming.producer.service;

import com.logmate.streaming.common.dto.SpringBootParsedLog;
import com.logmate.streaming.common.topic.KafkaTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class KafkaLogProducer {

  private final KafkaTemplate<String, SpringBootParsedLog> kafkaTemplate;

  public Mono<Void> sendLog(SpringBootParsedLog logMessage) {
    return Mono.fromRunnable(() ->
        kafkaTemplate.send(KafkaTopic.LOG_TOPIC, logMessage)
    );
  }
}
