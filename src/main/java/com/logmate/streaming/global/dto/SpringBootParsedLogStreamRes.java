package com.logmate.streaming.global.dto;

import com.logmate.streaming.global.log.LogType;
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
  private String timestamp;
  private String level;
  private String thread;
  private String logger;
  private String message;

  private String agentId;
  private String thNum;
  private LogType logType;
}
