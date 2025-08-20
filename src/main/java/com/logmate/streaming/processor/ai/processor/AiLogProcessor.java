package com.logmate.streaming.processor.ai.processor;

import com.logmate.streaming.common.constant.ai.AiConstant;
import com.logmate.streaming.common.dto.log.TomcatAccessParsedLog;
import com.logmate.streaming.common.dto.log.LogEnvelope;
import com.logmate.streaming.processor.ai.dto.AiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiLogProcessor {

  private final WebClient aiWebClient;
  private final AiConstant constant;

  public boolean supports(LogEnvelope env) {
    Object obj = env.getLog();
    return obj instanceof TomcatAccessParsedLog;
  }

  public Mono<LogEnvelope> process(LogEnvelope env) {
    if (!supports(env)) {
      log.info("[AI] failed, This processor does not support this log type:{}", env.getLogType());
      return Mono.just(env);
    }
    TomcatAccessParsedLog t = (TomcatAccessParsedLog) env.getLog();
    return aiWebClient.post()
        .uri(constant.getUri())
        .bodyValue(t)
        .retrieve()
        .bodyToMono(AiResponse.class)
        .doOnSuccess(ai -> log.info("[AI] success request, score: {}", ai.getScore()))
        .map(ai -> env.setAiScore(ai.getScore()))
        // AI 실패 시에도 파이프라인 흘러가게: 원본 로그를 WS로
        .onErrorResume(e -> {
          log.error("[AI] failed, fallback to raw WS payload", e);
          return Mono.just(env);
        });
  }

}