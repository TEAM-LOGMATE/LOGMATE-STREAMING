package com.logmate.streaming.processor.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TomcatAccessParsedLogWebSocketRes {

  private String logType;
  private TomcatAccessParsedLogWebSocketLogRes log;

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  public static class TomcatAccessParsedLogWebSocketLogRes {

    private String timestamp;
    private String method;
    private String url;
    private String protocol;
    private int statusCode;
    private int responseSize;
    private String referer;
    private String userAgent;
    private String ip;
  }

  private Float aiScore;
}
