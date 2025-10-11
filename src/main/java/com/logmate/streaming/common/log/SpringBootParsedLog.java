package com.logmate.streaming.common.log;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpringBootParsedLog implements ParsedLogData {
  private boolean isFormatCorrect;
  private Instant timestamp;
  private String level;
  private String thread;
  private String logger;
  private String message;
  private String userTimezone;
}
