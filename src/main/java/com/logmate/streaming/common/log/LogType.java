package com.logmate.streaming.common.log;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum LogType {
  SPRING_BOOT("springboot", SpringBootParsedLog.class),
  TOMCAT_ACCESS("tomcat", TomcatAccessParsedLog.class);

  private final String str;
  private final Class<? extends ParsedLogData> clazz;
}
