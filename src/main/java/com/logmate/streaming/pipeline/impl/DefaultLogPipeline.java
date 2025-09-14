package com.logmate.streaming.pipeline.impl;

import com.logmate.streaming.common.log.LogEnvelope;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultLogPipeline implements LogPipeline {

  private final List<LogProcessor> processors;

  @Override
  public Mono<Void> process(LogEnvelope env) {
    // order 기준 정렬 후 그룹핑
    return Flux.fromIterable(processors.stream()
            .collect(Collectors.groupingBy(
                LogProcessor::getOrder,
                TreeMap::new,
                Collectors.toList()
            ))
            .values()) // 순서대로 그룹 꺼냄
        .concatMap(group -> {
          // 동일 order → 병렬 실행
          List<Mono<LogEnvelope>> tasks = group.stream()
              .filter(p -> p.supports(env))
              .map(p -> p.process(env))
              .toList();

          return Mono.when(tasks).thenReturn(env);
        })
        .then();
  }
}
