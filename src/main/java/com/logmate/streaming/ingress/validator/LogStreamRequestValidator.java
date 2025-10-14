package com.logmate.streaming.ingress.validator;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.InvalidRequestException;

@Slf4j
public class LogStreamRequestValidator {

  public static void validate(String agentId, String thNum) {
    if (agentId == null || agentId.isBlank()) {
      throw new InvalidRequestException("Agent ID must not be blank");
    }
    if (!agentId.matches("^[a-fA-F0-9\\-]{36}$")) {  // UUID 형태
      throw new InvalidRequestException("Agent ID format invalid");
    }
    if (thNum == null || thNum.isBlank()) {
      throw new InvalidRequestException("Thread number must not be blank");
    }
    if (!thNum.matches("\\d+")) {
      throw new InvalidRequestException("Thread number must be numeric");
    }
    int i = Integer.parseInt(thNum);
    if (i < 0 || i > 65535) {
      throw new InvalidRequestException("Thread number must be between 0 and 65535");
    }
    log.debug("[LogStreamRequestValidator] Validated request. agentId={}, thNum={}", agentId,
        thNum);
  }
}
