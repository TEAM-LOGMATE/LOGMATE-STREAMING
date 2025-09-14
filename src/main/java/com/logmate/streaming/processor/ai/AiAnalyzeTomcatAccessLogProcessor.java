package com.logmate.streaming.processor.ai;

import com.logmate.streaming.common.constant.ai.AiConstant;
import com.logmate.streaming.common.dto.log.TomcatAccessParsedLog;
import com.logmate.streaming.common.dto.log.LogEnvelope;
import com.logmate.streaming.processor.LogProcessor;
import com.logmate.streaming.processor.ai.client.AiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class AiAnalyzeTomcatAccessLogProcessor implements LogProcessor {

  private final AiClient aiClient;
  private final AiConstant constant;
  private final int order;

  @Override
  public int getOrder() {
    return order;
  }

  /**
   * 해당 로그가 AI 처리 대상인지 여부를 반환한다.
   */
  @Override
  public boolean supports(LogEnvelope env) {
    return env.getLog() instanceof TomcatAccessParsedLog;
  }

  /**
   * AI 서버에 로그를 전달하여 분석 점수를 얻는다.
   * - Tomcat Access Log만 지원
   * - 실패 시 원본 로그를 그대로 반환 (fallback)
   */
  @Override
  public Mono<LogEnvelope> process(LogEnvelope env) {
    TomcatAccessParsedLog t = (TomcatAccessParsedLog) env.getLog();

    return aiClient.analyze(t, constant.getPath())
        .doOnSubscribe(sub -> log.info("[AI] Sending Tomcat log. agentId={}", env.getAgentId()))
        .map(ai -> env.setAiScore(ai.getScore()))
        .onErrorResume(e -> {
          log.error("[AI] Tomcat analysis failed. agentId={}, error={}", env.getAgentId(), e.getMessage(), e);
          return Mono.just(env);
        });
  }

}