package com.logmate.streaming.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.logmate.streaming.common.dto.SpringBootParsedLogStreamRes;
import com.logmate.streaming.common.dto.TomcatAccessParsedLogStreamRes;
import com.logmate.streaming.common.log.LogType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class JsonNodeMapper {
  private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

  public static SpringBootParsedLogStreamRes toSpringBootParsedLogStreamRes(JsonNode node) {
    if (node == null || node.isNull()) {
      throw new IllegalArgumentException("source JsonNode must not be null");
    }

    JsonNode logNode = node.path("log");
    if (logNode.isMissingNode()) {
      throw new IllegalArgumentException("missing 'log' field in source node");
    }

    SpringBootParsedLogStreamRes res = new SpringBootParsedLogStreamRes();

    // ===== log 내부 필드 =====
    res.setFormatCorrect(logNode.path("formatCorrect").asBoolean(false));
    res.setLevel(logNode.path("level").asText(null));
    res.setThread(logNode.path("thread").asText(null));
    res.setLogger(logNode.path("logger").asText(null));
    res.setMessage(logNode.path("message").asText(null));

    // ===== timezone 변환 =====
    String timestampStr = logNode.path("timestamp").asText(null);
    String timezoneStr = logNode.path("userTimezone").asText("UTC"); // 없으면 기본 UTC
    try {
      if (timestampStr != null && !timestampStr.isEmpty()) {
        Instant utcInstant = Instant.from(ISO_FORMATTER.parse(timestampStr));
        ZoneId userZone = ZoneId.of(timezoneStr);
        res.setTimestamp(LocalDateTime.ofInstant(utcInstant, userZone).truncatedTo(ChronoUnit.SECONDS)); // user timezone 변환
      }
    } catch (Exception e) {
      res.setTimestamp(null);
    }

    // ===== 상위 필드 =====
    res.setAgentId(node.path("agentId").asText(null));
    res.setThNum(node.path("thNum").asText(null));

    String logTypeStr = node.path("logType").asText(null);
    if (logTypeStr != null) {
      try {
        res.setLogType(LogType.valueOf(logTypeStr));
      } catch (IllegalArgumentException e) {
        res.setLogType(null);
      }
    }

    return res;
  }

  public static TomcatAccessParsedLogStreamRes toTomcatAccessParsedLogStreamRes(JsonNode node) {
    if (node == null || node.isNull()) {
      throw new IllegalArgumentException("source JsonNode must not be null");
    }

    JsonNode logNode = node.path("log");
    if (logNode.isMissingNode()) {
      throw new IllegalArgumentException("missing 'log' field in source node");
    }

    TomcatAccessParsedLogStreamRes res = new TomcatAccessParsedLogStreamRes();

    // ===== log 내부 필드 =====
    res.setFormatCorrect(logNode.path("formatCorrect").asBoolean(false));
    res.setIp(logNode.path("ip").asText(null));
    res.setMethod(logNode.path("method").asText(null));
    res.setUrl(logNode.path("url").asText(null));
    res.setProtocol(logNode.path("protocol").asText(null));
    res.setStatusCode(logNode.path("statusCode").asInt(0));
    res.setResponseSize(logNode.path("responseSize").asInt(0));
    res.setReferer(logNode.path("referer").asText(null));
    res.setUserAgent(logNode.path("userAgent").asText(null));
    res.setExtra(logNode.path("extra").asText(null));

    // ===== timezone 변환 (UTC → user timezone) =====
    String timestampStr = logNode.path("timestamp").asText(null);
    String timezoneStr = logNode.path("userTimezone").asText("UTC");

    try {
      if (timestampStr != null && !timestampStr.isEmpty()) {
        Instant utcInstant = Instant.from(ISO_FORMATTER.parse(timestampStr));
        ZoneId userZone = ZoneId.of(timezoneStr);
        // user timezone 변환 + 소수점(밀리초) 제거
        res.setTimestamp(LocalDateTime.ofInstant(utcInstant, userZone).truncatedTo(ChronoUnit.SECONDS));
      }
    } catch (Exception e) {
      res.setTimestamp(null);
    }

    // ===== 상위 필드 =====
    res.setAgentId(node.path("agentId").asText(null));
    res.setThNum(node.path("thNum").asText(null));
    res.setAiScore((float) node.path("aiScore").asDouble(0.0));

    String logTypeStr = node.path("logType").asText(null);
    if (logTypeStr != null) {
      try {
        res.setLogType(LogType.valueOf(logTypeStr));
      } catch (IllegalArgumentException e) {
        res.setLogType(null);
      }
    }

    return res;
  }
}
