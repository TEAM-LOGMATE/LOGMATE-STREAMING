package com.logmate.streaming.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logmate.streaming.common.constant.log.LogType;
import com.logmate.streaming.common.dto.log.SpringBootParsedLog;
import com.logmate.streaming.common.dto.log.TomcatAccessParsedLog;
import com.logmate.streaming.common.dto.log.LogEnvelope;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogParserUtil {

  private static final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

  public static LogEnvelope parse(String json) throws Exception {
    JsonNode root = om.readTree(json);
    LogType logType = LogType.valueOf(root.get("logType").asText());
    String agentId = root.path("agentId").asText(null);
    String thNum   = root.path("thNum").asText(null);

    JsonNode logNode = root.get("log");

    Object log = switch (logType) {
      case SPRING_BOOT -> om.treeToValue(logNode, SpringBootParsedLog.class);
      case TOMCAT_ACCESS -> om.treeToValue(logNode, TomcatAccessParsedLog.class);
      default -> throw new IllegalArgumentException("Unknown logType: " + logType);
    };

    return new LogEnvelope(logType.getName(), log, agentId, thNum, null);
  }
}
