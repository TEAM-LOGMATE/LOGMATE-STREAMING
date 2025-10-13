package com.logmate.streaming.common.dto;

import com.logmate.streaming.common.log.LogType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class SpringBootParsedLogStreamRes {
  private boolean formatCorrect;
  private LocalDateTime timestamp;
  private String level;
  private String thread;
  private String logger;
  private String message;

  private String agentId;
  private String thNum;
  private LogType logType;
}
