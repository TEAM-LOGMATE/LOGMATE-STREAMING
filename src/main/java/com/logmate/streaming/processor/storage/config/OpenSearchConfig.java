package com.logmate.streaming.processor.storage.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logmate.streaming.global.constant.opensearch.OpenSearchConstant;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenSearchConfig {

  private final OpenSearchConstant openSearchConstant;

  @Bean
  public OpenSearchClient openSearchClient() {
    RestClient restClient = RestClient.builder(
        new HttpHost(openSearchConstant.server.HOST, openSearchConstant.server.PORT, openSearchConstant.server.SCHEME)
    ).build();

    ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 문자열로 직렬화


    JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(objectMapper);

    return new OpenSearchClient(new RestClientTransport(restClient, jsonpMapper));
  }
}
