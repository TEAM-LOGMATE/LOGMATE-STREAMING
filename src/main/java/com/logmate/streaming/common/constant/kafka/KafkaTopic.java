package com.logmate.streaming.common.constant.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaTopic {

  @Value("${kafka.topic.log}")
  public String LOG_TOPIC;
}
