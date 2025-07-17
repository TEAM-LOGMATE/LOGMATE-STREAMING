package com.logmate.streaming.consumer.storage.service;

import com.logmate.streaming.common.dto.SpringBootParsedLog;

public interface LogStorageService {
  void storeLog(SpringBootParsedLog logData);
}
