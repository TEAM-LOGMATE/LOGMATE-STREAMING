package com.logmate.streaming.common.log;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SpringBootParsedLog {

  private boolean isFormatCorrect;
  private LocalDateTime timestamp;
  private String level;
  private String thread;
  private String logger;
  private String message;
  private String userCode;
}
