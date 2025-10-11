package com.logmate.streaming.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logmate.streaming.common.log.LogType;
import com.logmate.streaming.common.log.ParsedLogData;
import com.logmate.streaming.common.log.LogEnvelope;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그 메시지를 JSON 문자열에서 LogEnvelope 객체로 변환하는 유틸리티 클래스.
 *
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogParserUtil {

  private static final ObjectMapper om = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


  public static LogEnvelope parse(String json) throws Exception {
    JsonNode root = om.readTree(json);

    LogType logType = LogType.valueOf(root.get("logType").asText());
    Class<?> targetClass = logType.getClazz();

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
    if (!(logData instanceof ParsedLogData)) {
      log.error("[LogParserUtil] Failed to parse log. logType={}, agentId={}, raw={}",
          logType, agentId, json);
      throw new IllegalArgumentException("Log parsing failed for type: " + logType);
    }

    return new LogEnvelope(logType,(ParsedLogData) logData, agentId, thNum, null);
  }
}
