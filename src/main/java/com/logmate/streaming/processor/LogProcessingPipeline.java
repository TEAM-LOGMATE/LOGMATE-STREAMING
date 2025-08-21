package com.logmate.streaming.processor;

import com.logmate.streaming.common.dto.log.LogEnvelope;
import com.logmate.streaming.processor.ai.processor.AiLogProcessor;
import com.logmate.streaming.processor.storage.processor.LogStorageProcessor;
import com.logmate.streaming.processor.ws.processor.WebSocketProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LogProcessingPipeline {

  private final AiLogProcessor ai;              // 반드시 먼저
  private final WebSocketProcessor ws;       // AI 이후
  private final LogStorageProcessor storage;    // AI 이후

  public Mono<Void> process(LogEnvelope env) {
    return ai.process(env)                             // 1) AI 먼저
        .flatMap(after -> Mono.when(                  // 2) 이후 WS + Storage 병렬
            ws.process(after).onErrorResume(e -> Mono.empty()),
            storage.process(after).onErrorResume(e -> Mono.empty())
        ))
        .then();
  }
}
