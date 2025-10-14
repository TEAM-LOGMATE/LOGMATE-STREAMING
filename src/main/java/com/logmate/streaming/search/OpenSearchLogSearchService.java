package com.logmate.streaming.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.logmate.streaming.global.constant.opensearch.OpenSearchConstant;
import com.logmate.streaming.global.log.LogType;
import com.logmate.streaming.global.util.JsonNodeMapper;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenSearchLogSearchService {

  private final OpenSearchRepository repository;
  private final OpenSearchConstant constant;

  /**
   * OpenSearch 로그 검색 (동기)
   */
  public List<?> searchLogs(String agentId,
      Integer thNum,
      LogType logType,
      Instant startTime,
      Instant endTime) {

    List<JsonNode> logs = repository.findByAgentIdAndThNumAndLogTypeWithinTimeRange(
        agentId, thNum, logType, startTime,
        endTime, constant);

    return switch (logType) {
      case SPRING_BOOT ->
          logs.stream().map(JsonNodeMapper::toSpringBootParsedLogStreamRes).toList();
      case TOMCAT_ACCESS ->
          logs.stream().map(JsonNodeMapper::toTomcatAccessParsedLogStreamRes).toList();
      default -> throw new IllegalArgumentException("Unsupported logType: " + logType);
    };
  }
}
