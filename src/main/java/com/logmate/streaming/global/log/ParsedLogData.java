package com.logmate.streaming.global.log;

import java.time.Instant;

public interface ParsedLogData {
  String getMessage();
  String getUserTimezone();
  Instant getTimestamp();
  boolean isFormatCorrect();
}
