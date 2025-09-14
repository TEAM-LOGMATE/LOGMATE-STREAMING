package com.logmate.streaming.processor.ai.processor;

import com.logmate.streaming.common.constant.ai.AiConstant;
import com.logmate.streaming.common.dto.log.TomcatAccessParsedLog;
import com.logmate.streaming.common.dto.log.LogEnvelope;
import com.logmate.streaming.processor.LogProcessor;
import com.logmate.streaming.processor.ai.dto.AiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class AiLogProcessor implements LogProcessor {

  private final WebClient aiWebClient;
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
        .doOnSubscribe(sub -> log.info("[AI] Sending log to AI server. agentId={}, logType={}",
            env.getAgentId(), env.getLogType()))
        .doOnSuccess(ai -> log.info("[AI] Response received. agentId={}, logType={}, score={}",
            env.getAgentId(), env.getLogType(), ai.getScore()))
        .map(ai -> env.setAiScore(ai.getScore()))
        .onErrorResume(e -> {
          log.error("[AI] Request failed. agentId={}, logType={}, error={}",
              env.getAgentId(), env.getLogType(), e.getMessage(), e);
          return Mono.just(env); // fallback: 점수 없이 원본 로그 반환
        });
  }

}