package com.logmate.streaming.processor.storage;

import com.logmate.streaming.common.log.LogEnvelope;
import com.logmate.streaming.processor.LogProcessor;
import com.logmate.streaming.processor.storage.service.LogStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
public class LogStorageProcessor implements LogProcessor {

  private final LogStorageService storage;
  private final int order;

  @Override
  public int getOrder() {
    return order;
  }

  @Override
  public boolean supports(LogEnvelope env) {
    return true;
  }

  @Override
  public Mono<LogEnvelope> process(LogEnvelope env) {
    return Mono.fromRunnable(() -> storage.storeLogEnvelope(env))
        .subscribeOn(Schedulers.boundedElastic()) // 블로킹 분리
        .doOnError(e -> log.error(
            "[LogStorageProcessor] Failed to store log. agentId={}, logType={}, error={}",
            env.getAgentId(), env.getLogType(), e.getMessage(), e
        ))
        .onErrorResume(e -> Mono.empty())
        .thenReturn(env);
  }
}
