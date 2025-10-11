package com.logmate.streaming.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpringBootParsedLogStreamReq {
  private boolean formatCorrect;
  private String timestamp;
  private String level;
  private String thread;
  private String logger;
  private String message;
  private String userTimezone;
}
