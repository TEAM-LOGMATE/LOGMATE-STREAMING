package com.logmate.streaming.processor.ws;

import com.logmate.streaming.global.log.LogEnvelope;
import com.logmate.streaming.global.util.WebSocketResponseMapper;
import com.logmate.streaming.processor.LogProcessor;
import com.logmate.streaming.processor.ws.handler.LogWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * WebSocketProcessor
 *
 * <p>로그를 WebSocket 구독자들에게 실시간 전송하는 Processor.
 * LogEnvelope 으로부터 응답 DTO를 생성한 뒤,
 * LogWebSocketHandler 를 통해 대상 세션(agentId + thNum)으로 브로드캐스트한다.
 *
 */
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
    Object response = switch (env.getLogType()) {
      case SPRING_BOOT -> WebSocketResponseMapper.toSpringBootRes(env);
      case TOMCAT_ACCESS -> WebSocketResponseMapper.toTomcatRes(env);
      default -> throw new IllegalArgumentException("Unsupported logType: " + env.getLogType());
    };
    return Mono.fromRunnable(() -> {
          wsHandler.push(env.getAgentId(), env.getThNum(), response);
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
