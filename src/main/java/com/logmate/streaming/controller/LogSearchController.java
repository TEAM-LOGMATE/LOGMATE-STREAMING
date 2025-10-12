package com.logmate.streaming.controller;

import com.logmate.streaming.common.log.LogType;
import com.logmate.streaming.search.OpenSearchLogSearchService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/streaming/logs")
@RequiredArgsConstructor
public class LogSearchController {
  private final OpenSearchLogSearchService logSearchService;

  /**
   * OpenSearch에서 agentId, thNum, logType, 시간 범위로 로그 조회
   */
  @GetMapping("/search")
  public ResponseEntity<?> searchLogs(
      @RequestParam String agentId,
      @RequestParam Integer thNum,
      @RequestParam LogType logType,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      Instant startTime,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      Instant endTime
  ) {
    // 기본값: 최근 6시간
    Instant now = Instant.now();
    Instant effectiveEnd = (endTime != null) ? endTime : now;
    Instant effectiveStart = (startTime != null) ? startTime : effectiveEnd.minus(6, ChronoUnit.HOURS);

    return ResponseEntity.ok(
        logSearchService.searchLogs(agentId, thNum, logType, effectiveStart, effectiveEnd)
    );
  }
}
