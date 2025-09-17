package com.logmate.streaming.processor.storage.service;

import com.logmate.streaming.common.constant.opensearch.OpenSearchConstant;
import com.logmate.streaming.common.log.LogEnvelope;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenSearchLogStorageService implements LogStorageService {

  private final OpenSearchClient client;
  private final OpenSearchConstant constant;

  @Override
  public void storeLogEnvelope(LogEnvelope env) {
    try {
      String index = buildIndexName(env); // 예: log-tomcat-2025.08.20
      IndexRequest.Builder<LogEnvelope> b = new IndexRequest.Builder<LogEnvelope>()
          .index(index)
          .document(env);

      if (env.getAgentId() != null) {
        b.routing(env.getAgentId()); // agent 단위 로컬리티
      }

      IndexResponse res = client.index(b.build());
      log.info("[OpenSearch] stored: {} -> {}", index, res.result());
    } catch (Exception e) {
      log.error("[OpenSearch] storeEnvelope failed", e);
    }
  }

  private String buildIndexName(LogEnvelope env) {
    String date = java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd")
        .withZone(java.time.ZoneOffset.UTC).format(Instant.now());
    return constant.index.LOG + "-" + env.getLogType() + "-" + date;
  }
}
