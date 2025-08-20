package com.logmate.streaming.processor.ws.processor;

import com.logmate.streaming.common.dto.log.LogEnvelope;
import com.logmate.streaming.processor.ws.handler.LogWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WebSocketProcessor {

  private final LogWebSocketHandler wsHandler;

  public Mono<Void> process(LogEnvelope env) {
    return Mono.fromRunnable(() -> {
      wsHandler.push(env.getAgentId(), env.getThNum(), env);
    }).then();
  }
}
