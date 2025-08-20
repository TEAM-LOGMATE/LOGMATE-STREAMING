package com.logmate.streaming.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.logmate.streaming.common.constant.kafka.KafkaConstant;
import com.logmate.streaming.common.constant.log.LogType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenericKafkaLogProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final KafkaConstant kafkaConstant;
  private final ObjectMapper objectMapper;

  public Mono<Void> sendLog(Object logData, LogType logType, String agentId, String thNum) {
    return Mono.fromRunnable(() -> {
      try {
        // POJO를 바로 트리로 변환 (LDT -> ISO 문자열로 반영됨)
        ObjectNode root = objectMapper.createObjectNode()
            .set("log", objectMapper.valueToTree(logData));// <-- putPOJO 대신 valueToTree
        root.put("logType", logType.name());
        root.put("agentId", agentId);
        root.put("thNum", thNum);

        // 반드시 ObjectMapper로 write (JsonNode.toString() 금지)
        String json = objectMapper.writeValueAsString(root);

        kafkaTemplate.send(kafkaConstant.topic.LOG_TOPIC, json);
        log.info("[GenericKafkaLogProducer] Sent log to Kafka: {}", json);
      } catch (Exception e) {
        log.error("[GenericKafkaLogProducer] Failed to send log to Kafka", e);
      }
    });
  }
}
