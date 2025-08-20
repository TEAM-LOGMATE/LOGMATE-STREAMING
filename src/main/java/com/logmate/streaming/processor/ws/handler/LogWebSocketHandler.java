package com.logmate.streaming.processor.ws.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogWebSocketHandler implements WebSocketHandler {

  private final ObjectMapper objectMapper;

  // 채널별 브로드캐스트 sink: key = agentId:thNum
  private final Map<String, Many<String>> channels = new ConcurrentHashMap<>();

  @Override
  public Mono<Void> handle(WebSocketSession session) {
    String path = session.getHandshakeInfo().getUri().getPath();
    // 기대 경로: /ws/logs/{agentId}/{thNum}
    String[] seg = path.split("/");
    String agentId = seg.length > 3 ? dec(seg[3]) : "unknown";
    String thNum   = seg.length > 4 ? dec(seg[4]) : "0";
    String key = key(agentId, thNum);

    Sinks.Many<String> sink = channels.computeIfAbsent(key,
        k -> {
          log.info("[LogWebSocketHandler] New channel created for key: {}", k); // 채널 생성 시 로그
          return Sinks.many().multicast().directBestEffort();
        });

    Flux<WebSocketMessage> out = sink.asFlux()
        .doOnSubscribe(subscription -> log.info("[LogWebSocketHandler] WebSocket connection established for key: {}", key)) // 연결 성공 시 로그
        .map(session::textMessage)
        .doFinally(signalType -> {
          // 연결 종료 시 로직 (예: 연결된 클라이언트 수 확인 후 채널 제거)
          log.info("[LogWebSocketHandler] WebSocket connection closed for key: {}", key);
        });
    // 클라이언트가 보낸 메시지는 사용하지 않으니 무시.
    // 연결 종료 시 정리 로직이 필요하면 doFinally에서 subscriber 카운트 줄이고 제거.
    return session.send(out);
  }

  // 파이프라인에서 호출: 해당 채널로만 push
  public void push(String agentId, String thNum, Object payload) {
    String key = key(agentId, thNum);
    Sinks.Many<String> sink = channels.get(key(agentId, thNum));
    if (sink != null) {
      Sinks.EmitResult result = sink.tryEmitNext(toJson(payload));
      if (result.isSuccess()) {
        log.info("[LogWebSocketHandler] Successfully pushed message to channel: {}", key); // 푸시 성공 시 로그
      } else {
        log.warn("[LogWebSocketHandler] Failed to push message to channel: {}, result: {}", key, result); // 푸시 실패 시 로그
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
      return "{\"error\":\"json-serialize\"}";
    }
  }
}
