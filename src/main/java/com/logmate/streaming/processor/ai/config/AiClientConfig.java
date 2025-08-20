package com.logmate.streaming.processor.ai.config;

import com.logmate.streaming.common.constant.ai.AiConstant;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class AiClientConfig {

  @Bean
  public WebClient aiWebClient(WebClient.Builder builder, AiConstant props) {
    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.connectionTimeoutMs)
        .responseTimeout(Duration.ofMillis(props.getReadTimeoutMs()));

    return builder
        .baseUrl(props.getBaseUrl())
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
  }
}
