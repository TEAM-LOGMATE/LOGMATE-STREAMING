package com.logmate.streaming.messaging.consumer.impl.kafka;

import com.logmate.streaming.global.log.LogEnvelope;
import com.logmate.streaming.messaging.consumer.LogConsumer;
import com.logmate.streaming.global.util.LogParserUtil;
import com.logmate.streaming.pipeline.LogPipeline;
import com.logmate.streaming.messaging.producer.DlqProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * KafkaLogConsumer
 *
 * Kafka에서 수집된 로그 메시지를 구독(consume)하고
 * LogProcessingPipeline을 통해 처리(AI → WS → Storage)한 뒤,
 * 처리 중 실패한 메시지는 DLQ(Dead Letter Queue)로 전송하는 Consumer 컴포넌트.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaLogConsumer implements LogConsumer {

  private final LogPipeline pipeline;
  private final DlqProducer dlqProducer;

  /**
   * Kafka 로그 토픽으로부터 메시지를 수신하는 메소드.
   * - 정상적으로 파싱된 로그는 pipeline.process()를 통해 처리된다.
   * - 파싱 실패 또는 파이프라인 실패 시 fallback()을 통해 DLQ로 전송된다.
   *
   * @param json Kafka에서 수신한 로그 메시지 (JSON 문자열)
   */
  @Override
  @KafkaListener(topics = "${kafka.topic.log}", groupId = "${spring.kafka.consumer.group-id}")
  public void consume(String json) {
    try {
      log.debug("[KafkaLogConsumer] Consumed log: {}", json);

      // JSON 문자열 → LogEnvelope 객체로 변환
      LogEnvelope env = LogParserUtil.parse(json);

      pipeline.process(env)
          // 처리 중 에러 발생 시 DLQ 전송 후 정상 종료
          .onErrorResume(ex -> {
            log.error("[KafkaLogConsumer] Pipeline processing failed. agentId={}, logType={}, error={}",
                env.getAgentId(), env.getLogType(), ex.getMessage(), ex);
            fallback(env.getAgentId(), json, ex);
            return Mono.empty();
          })
          .subscribe();
    } catch (Exception e) {
      // JSON 파싱 실패 시 DLQ 전송
      log.error("[KafkaLogConsumer] Failed to parse log message. rawSize={} bytes", json.length(), e);
      fallback(null, json, e);
    }
  }

  /**
   * Fallback 처리 메소드.
   * 파이프라인 실패/파싱 실패 시 DLQ Producer를 통해 실패 메시지를 전송한다.
   *
   * @param key     DLQ 전송에 사용할 메시지 키 (agentId, 없으면 null)
   * @param message 원본 메시지(JSON)
   * @param cause   실패 원인 Exception
   */
  private void fallback(String key, String message, Throwable cause) {
    // 실패한 메시지를 DLQ로 전송
    log.warn("[KafkaLogConsumer] Fallback → DLQ 전송 시작. key={}, cause={}",
        key != null ? key : "null", cause != null ? cause.getMessage() : "unknown");
    dlqProducer.send(key, message, cause);
  }
}
