package com.logmate.streaming.processor.ws.config;

import com.logmate.streaming.processor.ws.handler.LogWebSocketHandler;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

@Configuration
public class WebSocketRouteConfig {

  @Bean
  public SimpleUrlHandlerMapping webSocketMapping(LogWebSocketHandler handler) {
    // Ant 패턴 사용 가능. 동적 변수는 직접 파싱해야 하므로 /** 로 받는다.
    var mapping = new SimpleUrlHandlerMapping();
    mapping.setOrder(-1);
    mapping.setUrlMap(Map.of(
        "/ws/logs/**", handler   // /ws/logs/{agentId}/{thNum}
    ));
    return mapping;
  }

  @Bean
  public WebSocketHandlerAdapter webSocketHandlerAdapter() {
    return new WebSocketHandlerAdapter();
  }
}
