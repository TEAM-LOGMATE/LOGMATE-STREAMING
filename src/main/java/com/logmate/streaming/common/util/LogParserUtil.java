package com.logmate.streaming.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logmate.streaming.common.log.LogType;
import com.logmate.streaming.common.log.SpringBootParsedLog;
import com.logmate.streaming.common.log.TomcatAccessParsedLog;
import com.logmate.streaming.common.log.LogEnvelope;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그 메시지를 JSON 문자열에서 LogEnvelope 객체로 변환하는 유틸리티 클래스.
 *
 * 새로운 로그 타입이 추가될 경우 LOG_TYPE_MAP에 DTO 매핑만 등록하면 된다.
 * 예: LogType.NEW_TYPE, NewParsedLog.class
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogParserUtil {

  private static final ObjectMapper om = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  /**
   * 새로운 로그 타입이 추가될 경우 LOG_TYPE_MAP에 DTO 매핑 등록.
   * 예: LogType.NEW_TYPE, NewParsedLog.class
   */
  private static final Map<LogType, Class<?>> LOG_TYPE_MAP = Map.of(
      LogType.SPRING_BOOT, SpringBootParsedLog.class,
      LogType.TOMCAT_ACCESS, TomcatAccessParsedLog.class
  );

  public static LogEnvelope parse(String json) throws Exception {
    JsonNode root = om.readTree(json);

    LogType logType = LogType.valueOf(root.get("logType").asText());
    Class<?> targetClass = LOG_TYPE_MAP.get(logType);
    if (targetClass == null) {
      throw new IllegalArgumentException("Unsupported logType: " + logType);
    }

    String agentId = root.path("agentId").asText(null);
    String thNum   = root.path("thNum").asText(null);
    Object logData = om.treeToValue(root.get("log"), targetClass);

    if (logData == null) {
      log.error("[LogParserUtil] Failed to parse log. logType={}, agentId={}, raw={}",
          logType, agentId, json);
      throw new IllegalArgumentException("Log parsing failed for type: " + logType);
    }

    return new LogEnvelope(logType.getName(), logData, agentId, thNum, null);
  }
}
