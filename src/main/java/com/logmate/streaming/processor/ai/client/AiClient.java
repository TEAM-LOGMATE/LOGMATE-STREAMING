package com.logmate.streaming.processor.ai.client;

import com.logmate.streaming.processor.ai.dto.AiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AiClient {

  private final WebClient aiWebClient;

  public Mono<AiResponse> analyze(Object payload, String path) {
    return aiWebClient.post()
        .uri(path)
        .bodyValue(payload)
        .retrieve()
        .bodyToMono(AiResponse.class);
  }
}
