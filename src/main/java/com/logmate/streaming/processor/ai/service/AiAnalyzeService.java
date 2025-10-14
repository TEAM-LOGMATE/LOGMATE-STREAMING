package com.logmate.streaming.processor.ai.service;

import com.logmate.streaming.global.constant.ai.AiConstant;
import com.logmate.streaming.global.log.LogEnvelope;
import com.logmate.streaming.global.log.TomcatAccessParsedLog;
import com.logmate.streaming.processor.ai.client.AiClient;
import com.logmate.streaming.processor.ai.dto.AiAnalyzeTomcatAccessLogRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * AiAnalysisService
 *
 * Tomcat Access Log를 AI 서버로 전달하여 이상 탐지 점수를 계산하는 서비스.
 * 실패 시 원본 LogEnvelope을 그대로 반환하여 파이프라인 중단을 방지한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalyzeService {

  private final AiClient aiClient;
  private final AiConstant aiConstant;

  /**
   * Tomcat Access 로그를 AI 서버에 분석 요청하고 점수를 반영한다.
   *
   * @param env 로그 래퍼
   * @return 분석된 로그 (AI 점수 포함)
   */
  public Mono<LogEnvelope> analyzeTomcatLog(LogEnvelope env) {
    if (!(env.getLog() instanceof TomcatAccessParsedLog t)) {
      return Mono.just(env); // 해당 타입이 아니면 그대로 반환
    }

    AiAnalyzeTomcatAccessLogRequest req = AiAnalyzeTomcatAccessLogRequest.from(t);

    return aiClient.analyze(req, aiConstant.getPath())
        .doOnSubscribe(sub -> log.info("[AiAnalysisService] Sending Tomcat log. agentId={}", env.getAgentId()))
        .map(ai -> {
          env.setAiScore(ai.getScore());
          log.debug("[AiAnalysisService] AI score applied. agentId={}, score={}", env.getAgentId(), ai.getScore());
          return env;
        })
        .onErrorResume(e -> {
          log.error("[AiAnalysisService] AI analysis failed. agentId={}, error={}",
              env.getAgentId(), e.getMessage(), e);
          return Mono.just(env);
        });
  }
}