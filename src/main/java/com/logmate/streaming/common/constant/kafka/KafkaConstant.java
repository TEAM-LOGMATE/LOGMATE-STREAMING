package com.logmate.streaming.common.constant.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConstant {

  public final KafkaTopic topic;
  public final KafkaServer server;
}
