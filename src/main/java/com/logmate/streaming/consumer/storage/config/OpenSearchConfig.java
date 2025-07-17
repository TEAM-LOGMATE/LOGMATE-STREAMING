package com.logmate.streaming.consumer.storage.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logmate.streaming.common.constant.opensearch.OpenSearchConstant;
import com.logmate.streaming.common.constant.opensearch.OpenSearchServer;
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

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(objectMapper);

    return new OpenSearchClient(new RestClientTransport(restClient, jsonpMapper));
  }
}
