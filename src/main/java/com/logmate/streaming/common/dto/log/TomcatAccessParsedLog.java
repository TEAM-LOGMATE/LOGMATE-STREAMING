package com.logmate.streaming.common.dto.log;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TomcatAccessParsedLog {

  private boolean isFormatCorrect;
  private String ip;
  private LocalDateTime timestamp;
  private String method;
  private String url;
  private String protocol;
  private int statusCode;
  private int responseSize;
  private String referer;
  private String userAgent;
  private String extra;
}
