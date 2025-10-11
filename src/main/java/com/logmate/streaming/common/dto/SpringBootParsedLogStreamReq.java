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
public class SpringBootParsedLogStreamReq {
  private boolean formatCorrect;
  private String timestamp;
  private String level;
  private String thread;
  private String logger;
  private String message;
  private String userTimezone;
}
