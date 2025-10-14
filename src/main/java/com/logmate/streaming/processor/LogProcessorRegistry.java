package com.logmate.streaming.processor;

import com.logmate.streaming.processor.ai.AiAnalyzeTomcatAccessLogProcessor;
import com.logmate.streaming.processor.ai.service.AiAnalyzeService;
import com.logmate.streaming.processor.storage.LogStorageProcessor;
import com.logmate.streaming.processor.storage.service.LogStorageService;
import com.logmate.streaming.processor.ws.handler.LogWebSocketHandler;
import com.logmate.streaming.processor.ws.WebSocketProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 로그 처리 파이프라인에 참여하는 Processor들을 등록하는 클래스.
 *
 * 등록 방법
 * - LogProcessor 인터페이스를 구현한 구현체를 등록할 수 있다.
 * - 이 클래스에서 Bean으로 등록하고, 생성자에 order 값을 명시하여 실행 순서를 지정한다.
 *
 * 순서 규칙
 * - order 값이 낮을수록 먼저 실행된다.
 * - order 값이 같다면 병렬처리된다.
 */
@Configuration
public class LogProcessorRegistry {

  @Bean
  public LogProcessor aiProcessor(AiAnalyzeService analyzeService) {
    return new AiAnalyzeTomcatAccessLogProcessor( analyzeService, 0);
  }

  @Bean
  public LogProcessor wsProcessor(LogWebSocketHandler webSocketHandler) {
    return new WebSocketProcessor(webSocketHandler, 1);
  }

  @Bean
  public LogProcessor storageProcessor(LogStorageService storage) {
    return new LogStorageProcessor(storage, 1);
  }
}
