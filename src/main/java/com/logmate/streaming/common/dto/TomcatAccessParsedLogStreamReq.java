package com.logmate.streaming.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TomcatAccessParsedLogStreamReq {
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
  private String userTimezone;
}
