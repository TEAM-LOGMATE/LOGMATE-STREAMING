package com.logmate.streaming.global.dto;

import com.logmate.streaming.global.log.LogType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class TomcatAccessParsedLogStreamRes {
  private boolean formatCorrect;
  private String ip;
  private String timestamp;
  private String method;
  private String url;
  private String protocol;
  private int statusCode;
  private int responseSize;
  private String referer;
  private String userAgent;
  private String extra;

  private String agentId;
  private String thNum;
  private LogType logType;
  private Float aiScore;
}
