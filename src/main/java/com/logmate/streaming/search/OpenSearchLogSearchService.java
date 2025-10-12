package com.logmate.streaming.search;

import com.logmate.streaming.common.constant.opensearch.OpenSearchConstant;
import com.logmate.streaming.common.log.LogEnvelope;
import com.logmate.streaming.common.log.LogType;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenSearchLogSearchService {
  private final OpenSearchClient client;
  private final OpenSearchConstant constant;

  /**
   * OpenSearch 로그 검색 (동기)
   */
  public List<LogEnvelope> searchLogs(String agentId,
      Integer thNum,
      LogType logType,
      Instant startTime,
      Instant endTime) {
    try {

      // 인덱스 패턴 (예: log-spring_boot-logs-*, log-tomcat_access-logs-*)
      String indexPattern = constant.index.LOG + "-" + logType.getStr().toLowerCase() + "-" + constant.index.LOG + "-*";

      // 검색 조건
      Query query = Query.of(q -> q.bool(b -> b
          .must(m -> m.term(t -> t.field("agentId").value(FieldValue.of(agentId))))
          .must(m -> m.term(t -> t.field("thNum").value(FieldValue.of(thNum))))
          .must(m -> m.term(t -> t.field("logType.keyword").value(FieldValue.of(logType.name()))))
          .must(m -> m.range(r -> r
              .field("log.timestamp")
              .gte(JsonData.of(startTime.toString()))
              .lte(JsonData.of(endTime.toString()))
          ))
      ));
      log.info("[OpenSearch] query={}", query);
      // 검색 요청
      SearchRequest request = SearchRequest.of(s -> s
          .index(indexPattern)
          .size(1000) // 내부 기본값
          .sort(sort -> sort.field(f -> f.field("log.timestamp").order(SortOrder.Desc)))
          .query(query)
      );

      // 실행
      SearchResponse<LogEnvelope> response = client.search(request, LogEnvelope.class);

      log.info("[OpenSearch] query executed on pattern={} hits={}",
          indexPattern, response.hits().hits().size());

      return response.hits().hits()
          .stream()
          .map(hit -> hit.source())
          .toList();

    } catch (Exception e) {
      log.error("[OpenSearch] searchLogs failed", e);
      throw new RuntimeException("OpenSearch query failed", e);
    }
  }
}
