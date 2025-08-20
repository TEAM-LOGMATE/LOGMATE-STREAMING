package com.logmate.streaming.processor.storage.service;

import com.logmate.streaming.common.dto.log.LogEnvelope;

public interface LogStorageService {
  void storeLogEnvelope(LogEnvelope env);
}
