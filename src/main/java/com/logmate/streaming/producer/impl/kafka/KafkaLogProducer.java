package com.logmate.streaming.producer.impl.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.logmate.streaming.common.constant.kafka.KafkaConstant;
import com.logmate.streaming.common.log.LogType;
import com.logmate.streaming.common.log.ParsedLogData;
import com.logmate.streaming.common.util.ParsedLogMapper;
import com.logmate.streaming.producer.LogProducer;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaLogProducer implements LogProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final KafkaConstant kafkaConstant;
  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> sendLog(ParsedLogData logData, LogType logType, String agentId, String thNum) {
    return Mono.fromCallable(() -> {
          try {
            // 로그 데이터 표준화
            ObjectNode root = objectMapper.createObjectNode()
                .set("log", objectMapper.valueToTree(logData));
            root.put("logType", logType.name());
            root.put("agentId", agentId);
            root.put("thNum", thNum);

            // JSON 직렬화
            return objectMapper.writeValueAsString(root);
          } catch (Exception e) {
            throw new RuntimeException("Failed to serialize log data", e);
          }
        })
        .flatMap(json -> Mono.<Void>create(sink -> {
          try {
            // Kafka 전송
            CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(kafkaConstant.topic.LOG_TOPIC, agentId, json);
            
            // 로깅
            future.whenComplete((result, ex) -> {
              if (ex == null && result != null) {
                log.info(
                    "[GenericKafkaLogProducer] Log sent. size={} bytes, topic={}, partition={}, offset={}, agentId={}, logType={}",
                    json.length(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    agentId,
                    logType
                );
                sink.success();
              } else {
                log.error("[GenericKafkaLogProducer] Failed to send log. agentId={}, logType={}, error={}",
                    agentId, logType, ex != null ? ex.getMessage() : "unknown", ex);
                sink.error(ex != null ? ex : new RuntimeException("Kafka send failed: unknown error"));
              }
            });
          } catch (Exception e) {
            log.error("[GenericKafkaLogProducer] Unexpected error while sending log", e);
            sink.error(e);
          }
        }))
        .subscribeOn(Schedulers.boundedElastic()); // 직렬화 & 전송을 별도 쓰레드 풀에서 실행
  }
}
