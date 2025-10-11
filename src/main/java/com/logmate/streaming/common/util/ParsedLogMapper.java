package com.logmate.streaming.common.util;

import com.logmate.streaming.common.dto.SpringBootParsedLogStreamReq;
import com.logmate.streaming.common.dto.TomcatAccessParsedLogStreamReq;
import com.logmate.streaming.common.log.SpringBootParsedLog;
import com.logmate.streaming.common.log.TomcatAccessParsedLog;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParsedLogMapper {
  // ========== 공통 timestamp 파싱 로직 ==========
  private static final DateTimeFormatter STRICT_UTC_FORMATTER =
      new DateTimeFormatterBuilder()
          .appendPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
          .toFormatter()
          .withZone(ZoneOffset.UTC);

  private static Instant parseToInstant(String timestamp) {
    if (timestamp == null || timestamp.isBlank()) {
      throw new IllegalArgumentException("[ParsedLogMapper] timestamp must not be null or empty");
    }

    try {
      // "2025-10-10T12:30:45Z" ← 정확히 이 형식만 허용
      return Instant.from(STRICT_UTC_FORMATTER.parse(timestamp));
    } catch (DateTimeParseException e) {
      log.error("[ParsedLogMapper] Invalid timestamp format: {}", timestamp);
      throw new IllegalArgumentException(
          "Invalid timestamp format: expected ISO 8601 UTC (e.g. 2025-10-10T12:30:45Z)", e);
    }
  }

  // ========== 변환 메서드: Spring Boot 로그 ==========
  public static SpringBootParsedLog toEntity(SpringBootParsedLogStreamReq dto) {
    if (dto == null) return null;
    return SpringBootParsedLog.builder()
        .isFormatCorrect(dto.isFormatCorrect())
        .timestamp(parseToInstant(dto.getTimestamp()))
        .level(dto.getLevel())
        .thread(dto.getThread())
        .logger(dto.getLogger())
        .message(dto.getMessage())
        .userTimezone(dto.getUserTimezone())
        .build();
  }

  // ========== 변환 메서드: Tomcat Access 로그 ==========
  public static TomcatAccessParsedLog toEntity(TomcatAccessParsedLogStreamReq dto) {
    if (dto == null) return null;
    return TomcatAccessParsedLog.builder()
        .isFormatCorrect(dto.isFormatCorrect())
        .ip(dto.getIp())
        .timestamp(parseToInstant(dto.getTimestamp()))
        .method(dto.getMethod())
        .url(dto.getUrl())
        .protocol(dto.getProtocol())
        .statusCode(dto.getStatusCode())
        .responseSize(dto.getResponseSize())
        .referer(dto.getReferer())
        .userAgent(dto.getUserAgent())
        .extra(dto.getExtra())
        .userTimezone(dto.getUserTimezone())
        .build();
  }

}
