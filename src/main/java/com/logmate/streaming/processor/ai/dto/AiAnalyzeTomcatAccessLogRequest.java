package com.logmate.streaming.processor.ai.dto;

import com.logmate.streaming.global.log.TomcatAccessParsedLog;
import lombok.Data;

@Data
public class AiAnalyzeTomcatAccessLogRequest {
  private String method;
  private String url;
  private String statusCode;
  private String bytesSent;
  private String referer;
  private String userAgent;

  public static AiAnalyzeTomcatAccessLogRequest from(TomcatAccessParsedLog log) {
    AiAnalyzeTomcatAccessLogRequest req = new AiAnalyzeTomcatAccessLogRequest();
    req.setMethod(log.getMethod());
    req.setUrl(log.getUrl());
    req.setStatusCode(String.valueOf(log.getStatusCode()));
    req.setBytesSent(String.valueOf(log.getResponseSize()));
    req.setReferer(log.getReferer());
    req.setUserAgent(log.getUserAgent());
    return req;
  }
}
