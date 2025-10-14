package com.logmate.streaming.global.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEnvelope {

  private LogType logType;
  private ParsedLogData log;
  private String agentId;
  private String thNum;
  private Float aiScore;

  public LogEnvelope setAiScore(Float aiScore) {
    this.aiScore = aiScore;
    return this;
  }
}
