package com.logmate.streaming.consumer.storage.service;

import com.logmate.streaming.common.constant.opensearch.OpenSearchConstant;
import com.logmate.streaming.common.dto.SpringBootParsedLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenSearchLogStorageService implements LogStorageService{

  private final OpenSearchClient openSearchClient;
  private final OpenSearchConstant openSearchConstant;

  public void storeLog(SpringBootParsedLog logData) {
    try {
      IndexRequest<SpringBootParsedLog> request = new IndexRequest.Builder<SpringBootParsedLog>()
          .index(openSearchConstant.index.LOG)
          .document(logData)
          .build();

      IndexResponse response = openSearchClient.index(request);

      if (response.result() == Result.Created || response.result() == Result.Updated) {
        log.info("[OpenSearch] Log stored successfully: {}", response.id());
      } else {
        log.warn("[OpenSearch] Unexpected result storing log: {}", response.result());
      }
    } catch (Exception e) {
      log.error("[OpenSearch] Failed to store log", e);
    }
  }
}
