package com.logmate.streaming.common.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SpringBootParsedLogWebSocketRes {

  private String logType;
  private SpringBootParsedLogWebSocketLogRes log;

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  public static class SpringBootParsedLogWebSocketLogRes {

    private String timestamp;
    private String level;
    private String logger;
    private String message;
  }
}
