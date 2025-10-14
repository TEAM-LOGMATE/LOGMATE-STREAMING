package com.logmate.streaming.messaging.producer.impl.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.logmate.streaming.global.constant.kafka.KafkaConstant;
import com.logmate.streaming.global.log.LogType;
import com.logmate.streaming.global.log.ParsedLogData;
import com.logmate.streaming.messaging.producer.LogProducer;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * KafkaLogProducer
 *
 * 로그 스트림 데이터를 Kafka로 전송하는 Reactive 기반 Producer.
 * 로그는 표준 JSON 구조로 직렬화되어 Kafka 토픽으로 비동기 전송된다.
 *
 */
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
            // 로그 데이터 Json 표준화
            ObjectNode root = objectMapper.createObjectNode()
                .set("log", objectMapper.valueToTree(logData));
            root.put("logType", logType.name());
            root.put("agentId", agentId);
            root.put("thNum", thNum);

            // JSON 직렬화
            return objectMapper.writeValueAsString(root);
          } catch (Exception e) {
            throw new RuntimeException("[KafkaLogProducer] Failed to serialize log data", e);
          }
        })
        .flatMap(json -> Mono.<Void>create(sink -> {
          try {
            // Kafka 비동기 전송
            CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(kafkaConstant.topic.LOG_TOPIC, agentId, json);
            
            // 전송 결과 로깅
            future.whenComplete((result, ex) -> {
              if (ex == null && result != null) {
                log.info(
                    "[KafkaLogProducer] Log sent successfully. size={} bytes, topic={}, partition={}, offset={}, agentId={}, logType={}",
                    json.length(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    agentId,
                    logType
                );
                sink.success();
              } else {
                log.error(
                    "[KafkaLogProducer] Failed to send log. agentId={}, logType={}, error={}",
                    agentId,
                    logType,
                    ex != null ? ex.getMessage() : "unknown",
                    ex
                );
                sink.error(ex != null ? ex : new RuntimeException("Kafka send failed: unknown error"));
              }
            });
          } catch (Exception e) {
            log.error("[KafkaLogProducer] Unexpected error during send operation", e);
            sink.error(e);
          }
        }))
        // 직렬화 & Kafka 전송은 블로킹 가능성이 있으므로 별도 쓰레드 풀에서 실행
        .subscribeOn(Schedulers.boundedElastic());
  }
}
