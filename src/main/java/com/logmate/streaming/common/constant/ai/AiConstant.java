package com.logmate.streaming.common.constant.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "ai")
public class AiConstant {
  public String origin;
  public String path;
  public int connectionTimeoutMs;
  public int readTimeoutMs;
}
