package com.logmate.streaming.common.util;

import com.logmate.streaming.common.dto.SpringBootParsedLogWebSocketRes;
import com.logmate.streaming.common.dto.TomcatAccessParsedLogWebSocketRes;
import com.logmate.streaming.common.log.LogEnvelope;
import com.logmate.streaming.common.log.LogType;
import com.logmate.streaming.common.log.SpringBootParsedLog;
import com.logmate.streaming.common.log.TomcatAccessParsedLog;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class WebSocketResponseMapper {

  public static SpringBootParsedLogWebSocketRes toSpringBootRes(LogEnvelope envelope) {
    if (envelope == null || envelope.getLogType() != LogType.SPRING_BOOT) {
      return null;
    }

    SpringBootParsedLog log = (SpringBootParsedLog) envelope.getLog();

    SpringBootParsedLogWebSocketRes.SpringBootParsedLogWebSocketLogRes logRes =
        new SpringBootParsedLogWebSocketRes.SpringBootParsedLogWebSocketLogRes();

    String timezoneStr = Objects.requireNonNullElse(log.getUserTimezone(), "UTC");
    ZoneId userZone = ZoneId.of(timezoneStr);

    logRes.setTimestamp(LocalDateTime.ofInstant(log.getTimestamp(), userZone).toString());
    logRes.setLevel(log.getLevel());
    logRes.setLogger(log.getLogger());
    logRes.setMessage(log.getMessage());

    return SpringBootParsedLogWebSocketRes.builder()
        .logType(envelope.getLogType().name())
        .log(logRes)
        .build();
  }

  public static TomcatAccessParsedLogWebSocketRes toTomcatRes(LogEnvelope envelope) {
    if (envelope == null || envelope.getLogType() != LogType.TOMCAT_ACCESS) {
      return null;
    }

    TomcatAccessParsedLog log = (TomcatAccessParsedLog) envelope.getLog();

    TomcatAccessParsedLogWebSocketRes.TomcatAccessParsedLogWebSocketLogRes logRes =
        new TomcatAccessParsedLogWebSocketRes.TomcatAccessParsedLogWebSocketLogRes();

    String timezoneStr = Objects.requireNonNullElse(log.getUserTimezone(), "UTC");
    ZoneId userZone = ZoneId.of(timezoneStr);

    logRes.setTimestamp(LocalDateTime.ofInstant(log.getTimestamp(), userZone).toString());    logRes.setMethod(log.getMethod());
    logRes.setUrl(log.getUrl());
    logRes.setProtocol(log.getProtocol());
    logRes.setStatusCode(log.getStatusCode());
    logRes.setResponseSize(log.getResponseSize());
    logRes.setReferer(log.getReferer());
    logRes.setUserAgent(log.getUserAgent());
    logRes.setIp(log.getIp());

    return TomcatAccessParsedLogWebSocketRes.builder()
        .logType(envelope.getLogType().name())
        .log(logRes)
        .aiScore(envelope.getAiScore())
        .build();
  }
}
