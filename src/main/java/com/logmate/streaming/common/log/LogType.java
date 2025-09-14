package com.logmate.streaming.common.log;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum LogType {
  TOMCAT_ACCESS("tomcat"),
  SPRING_BOOT("springboot");

  private final String name;
}
