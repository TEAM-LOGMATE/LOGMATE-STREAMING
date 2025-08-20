package com.logmate.streaming.consumer;

import com.logmate.streaming.common.dto.log.LogEnvelope;
import com.logmate.streaming.processor.LogProcessingPipeline;
import com.logmate.streaming.common.util.LogParserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnifiedLogConsumer {

  private final LogProcessingPipeline pipeline;

  @KafkaListener(topics = "${kafka.topic.log}", groupId = "${spring.kafka.consumer.group-id}")
  public void consume(String json) {
    try {
      log.info("[UnifiedLogConsumer] consume log: {}", json);
      LogEnvelope env = LogParserUtil.parse(json);
      pipeline.process(env).subscribe();
    } catch (Exception e) {
      log.error("[UnifiedLogConsumer] parse failed: {}", json, e);
    }
  }
}
