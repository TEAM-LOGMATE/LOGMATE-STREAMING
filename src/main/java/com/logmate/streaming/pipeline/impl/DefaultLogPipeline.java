package com.logmate.streaming.pipeline.impl;

import com.logmate.streaming.global.log.LogEnvelope;
import com.logmate.streaming.pipeline.LogPipeline;
import com.logmate.streaming.processor.LogProcessor;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * DefaultLogPipeline
 *
 * 로그 처리 파이프라인의 기본 구현체.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultLogPipeline implements LogPipeline {

  private final List<LogProcessor> processors;

  @Override
  public Mono<Void> process(LogEnvelope env) {
    log.debug("[DefaultLogPipeline] Start processing log. agentId={}, logType={}, processors={}",
        env.getAgentId(), env.getLogType(), processors.size());
    // order 기준 정렬 후 그룹핑
    return Flux.fromIterable(processors.stream()
            .collect(Collectors.groupingBy(
                LogProcessor::getOrder,
                TreeMap::new,
                Collectors.toList()
            ))
            .values()) // 순서대로 그룹 꺼냄
        .concatMap(group ->
            Flux.fromIterable(group)
                .filter(p -> p.supports(env))
                .flatMap(p -> {
                  log.debug("[DefaultLogPipeline] Executing processor (order={}, type={})",
                      p.getOrder(), p.getClass().getSimpleName());
                  return p.process(env)
                      .doOnError(ex -> log.error(
                          "[DefaultLogPipeline] Processor failed (type={}, agentId={}, error={})",
                          p.getClass().getSimpleName(), env.getAgentId(), ex.getMessage(), ex));
                }, group.size()) // 병렬도 지정
                .then(Mono.just(env))
        )
        .doOnTerminate(() -> log.debug("[DefaultLogPipeline] Completed log processing. agentId={}, logType={}",
            env.getAgentId(), env.getLogType()))
        .then();
  }
}
