package com.logmate.streaming.processor.ws;

import com.logmate.streaming.common.dto.log.LogEnvelope;
import com.logmate.streaming.processor.LogProcessor;
import com.logmate.streaming.processor.ws.handler.LogWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class WebSocketProcessor implements LogProcessor {

  private final LogWebSocketHandler wsHandler;
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
    return Mono.fromRunnable(() -> {
          wsHandler.push(env.getAgentId(), env.getThNum(), env);
          log.debug("[WebSocketProcessor] Log pushed via WebSocket. agentId={}, thNum={}",
              env.getAgentId(), env.getThNum());
        })
        .onErrorResume(e -> {
          log.error("[WebSocketProcessor] Failed to push log. agentId={}, thNum={}, error={}",
              env.getAgentId(), env.getThNum(), e.getMessage(), e);
          return Mono.empty(); // 실패 시 파이프라인 끊기지 않게
        })
        .thenReturn(env); // 원본 env 반환해서 파이프라인 유지
  }
}
