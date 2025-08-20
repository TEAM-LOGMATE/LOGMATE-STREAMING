package com.logmate.streaming.processor.storage.processor;

import com.logmate.streaming.common.dto.log.LogEnvelope;
import com.logmate.streaming.processor.storage.service.OpenSearchLogStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class OpenSearchLogStorageProcessor {

  private final OpenSearchLogStorageService storage;

  public Mono<Void> process(LogEnvelope env) {
    return Mono.fromRunnable(() -> {
          storage.storeLogEnvelope(env);
        })
        .subscribeOn(Schedulers.boundedElastic()) // 블로킹 분리
        .then();
  }
}
