package com.logmate.streaming.common.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEnvelope {

  private String logType;   // springboot, tomcat
  private Object log;       // SpringBootParsedLog | TomcatAccessParsedLog
  private String agentId;
  private String thNum;
  private Float aiScore;

  public LogEnvelope setAiScore(Float aiScore) {
    this.aiScore = aiScore;
    return this;
  }
}
