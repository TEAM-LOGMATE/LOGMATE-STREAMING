package com.logmate.streaming.processor;

import com.logmate.streaming.common.log.LogEnvelope;
import reactor.core.publisher.Mono;

/**
 * LogProcessor
 *
 * 로그 처리기의 표준 인터페이스.
 *
 * 모든 Processor는 order 값을 가져 순서를 결정한다.
 * - order 값이 낮을수록 먼저 실행된다.
 * - 동일한 order 값의 Processor들은 병렬로 실행된다.
 *
 * 확장 가이드:
 *  - 새로운 로그 처리 단계를 추가하려면 이 인터페이스를 구현한다.
 *  - 예: Ai 처리 Processor, DB 저장 Processor 등
 */
public interface LogProcessor {
  int getOrder();
  boolean supports(LogEnvelope env);
  Mono<LogEnvelope> process(LogEnvelope env);
}
