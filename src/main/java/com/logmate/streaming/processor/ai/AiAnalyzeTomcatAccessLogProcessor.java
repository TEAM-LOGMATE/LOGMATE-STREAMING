package com.logmate.streaming.processor.ai;

import com.logmate.streaming.global.log.TomcatAccessParsedLog;
import com.logmate.streaming.global.log.LogEnvelope;
import com.logmate.streaming.processor.LogProcessor;
import com.logmate.streaming.processor.ai.service.AiAnalyzeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class AiAnalyzeTomcatAccessLogProcessor implements LogProcessor {

  private final AiAnalyzeService aiAnalyzeService;
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
    log.debug("[AiAnalyzeTomcatAccessLogProcessor] Processing log. agentId={}, logType={}",
        env.getAgentId(), env.getLogType());
    return aiAnalyzeService.analyzeTomcatLog(env);
  }

}