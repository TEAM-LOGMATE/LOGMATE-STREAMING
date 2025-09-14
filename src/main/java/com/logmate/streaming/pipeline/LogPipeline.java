package com.logmate.streaming.pipeline;

import com.logmate.streaming.common.dto.log.LogEnvelope;
import reactor.core.publisher.Mono;

/**
 * LogPipeline
 *
 * 로그 처리 파이프라인의 표준 인터페이스.
 *
 * 모든 파이프라인 구현체는 로그 메시지를 받아
 * 정의된 처리 순서에 따라 실행해야 한다.
 *
 * 책임:
 * - LogEnvelope 입력을 받아 처리한다.
 * - 처리 중 예외 발생 시 로그를 남기고 파이프라인을 중단한다.
 *
 * 기본 구현체 : DefaultLogPipeline
 */
public interface LogPipeline {
  Mono<Void> process(LogEnvelope env);
}
