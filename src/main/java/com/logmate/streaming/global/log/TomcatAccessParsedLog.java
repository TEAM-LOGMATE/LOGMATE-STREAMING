package com.logmate.streaming.global.log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TomcatAccessParsedLog implements ParsedLogData {
  private boolean isFormatCorrect;
  private String ip;
  private Instant timestamp;
  private String method;
  private String url;
  private String protocol;
  private int statusCode;
  private int responseSize;
  private String referer;
  private String userAgent;
  private String extra;
  private String userTimezone;

  @Override
  @JsonIgnore
  public String getMessage() {
    return String.format(
        "%s -- [%s] \"%s %s %s\" %d %d \"%s\" \"%s\" %s",
        ip,
        timestamp,
        method,
        url,
        protocol,
        statusCode,
        responseSize,
        referer,
        userAgent,
        extra
    );
  }
}
