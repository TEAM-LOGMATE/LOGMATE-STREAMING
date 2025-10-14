package com.logmate.streaming.processor.ws.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogWebSocketHandler implements WebSocketHandler {

  private final ObjectMapper objectMapper;

  /** agentId:thNum 별 브로드캐스트 sink */
  private final Map<String, Sinks.Many<String>> channels = new ConcurrentHashMap<>();

  /** agentId:thNum 별 활성 구독자 수 (동시 구독 지원용) */
  private final Map<String, AtomicInteger> subscriberCount = new ConcurrentHashMap<>();

  @Override
  public Mono<Void> handle(WebSocketSession session) {
    String path = session.getHandshakeInfo().getUri().getPath();
    // 기대 경로: /ws/logs/{agentId}/{thNum}
    String[] seg = path.split("/");
    String agentId = seg.length > 3 ? dec(seg[3]) : "unknown";
    String thNum = seg.length > 4 ? dec(seg[4]) : "0";
    String key = key(agentId, thNum);

    Sinks.Many<String> sink = channels.computeIfAbsent(key, k -> {
      log.info("[LogWebSocketHandler] New channel created for key: {}", k); // 채널 생성 시 로그
      return Sinks.many().multicast().directBestEffort();
    });

    // 구독자 수 증가
    subscriberCount.compute(key, (k, c) -> {
      if (c == null) {
        log.info("[WebSocketHandler] First subscriber connected. key={}", key);
        return new AtomicInteger(1);
      }
      int newCount = c.incrementAndGet();
      log.info("[WebSocketHandler] Subscriber joined. key={}, count={}", key, newCount);
      return c;
    });

    // Sink → WebSocketSession 으로 변환
    Flux<WebSocketMessage> out = sink.asFlux()
        .map(session::textMessage)
        .doOnSubscribe(sub -> log.info("[WebSocketHandler] WebSocket connection established. key={}", key))
        .doFinally(signal -> {
          // 구독자 수 감소 후 0이 되면 채널 제거
          subscriberCount.computeIfPresent(key, (k, c) -> {
            int newCount = c.decrementAndGet();
            if (newCount <= 0) {
              log.info("[WebSocketHandler] Last subscriber disconnected. key={}, channel removed", key);
              channels.remove(key);
              subscriberCount.remove(key);
            } else {
              log.info("[WebSocketHandler] Subscriber left. key={}, remaining={}", key, newCount);
            }
            return c;
          });
        });

    return session.send(out)
        .onErrorResume(e -> {
          log.error("[WebSocketHandler] Session error. key={}, error={}", key, e.getMessage(), e);
          channels.remove(key);
          subscriberCount.remove(key);
          return Mono.empty();
        });
  }

  /** 파이프라인에서 호출: 해당 채널로 메시지 push */
  public void push(String agentId, String thNum, Object payload) {
    String key = key(agentId, thNum);
    Sinks.Many<String> sink = channels.get(key);

    if (sink == null) {
      log.debug("[WebSocketHandler] No active channel for key={}, message dropped", key);
      return;
    }

    String json = toJson(payload);
    Sinks.EmitResult result = sink.tryEmitNext(json);

    if (result.isSuccess()) {
      log.debug("[WebSocketHandler] Message pushed. key={}, size={} bytes", key, json.length());
    } else {
      log.warn("[WebSocketHandler] Failed to push message. key={}, result={}", key, result);
      if (result == Sinks.EmitResult.FAIL_TERMINATED || result == Sinks.EmitResult.FAIL_CANCELLED) {
        log.info("[WebSocketHandler] Sink terminated, cleaning up. key={}", key);
        channels.remove(key);
        subscriberCount.remove(key);
      }
    }
  }

  private String key(String agentId, String thNum) {
    return agentId + ":" + thNum;
  }

  private String dec(String s) {
    return URLDecoder.decode(s, StandardCharsets.UTF_8);
  }

  private String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      log.error("[WebSocketHandler] Failed to serialize JSON payload", e);
      return "{\"error\":\"json-serialize\"}";
    }
  }
}
