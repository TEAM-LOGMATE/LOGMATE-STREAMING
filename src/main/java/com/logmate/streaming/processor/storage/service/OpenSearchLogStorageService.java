package com.logmate.streaming.processor.storage.service;

import com.logmate.streaming.global.constant.opensearch.OpenSearchConstant;
import com.logmate.streaming.global.log.LogEnvelope;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

      // event_time 기준 날짜 계산
      LocalDate eventDate = env.getLog().getTimestamp()
          .atZone(ZoneOffset.UTC)
          .toLocalDate();

      LocalDate today = LocalDate.now(ZoneOffset.UTC);

      long daysOld = ChronoUnit.DAYS.between(eventDate, today);
      if (daysOld > 30) {
        log.info("Skipped storing old log ({} days old)", daysOld);
        return; // 30일 초과 데이터는 저장하지 않음
      }

      String index = buildIndexName(env); // 예: logmate-tomcat-logs-2025.08.20
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
    String date = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        .withZone(java.time.ZoneOffset.UTC)
        .format(env.getLog().getTimestamp());

    return String.format(
        "%s-%s-%s-%s",
        constant.index.PREFIX,
        env.getLogType().getStr().toLowerCase(),
        constant.index.SUFFIX,
        date
    );
  }
}
