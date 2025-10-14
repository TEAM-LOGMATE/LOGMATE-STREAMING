package com.logmate.streaming.global.constant.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConstant {

  @Value("${spring.kafka.consumer.group-id}")
  public String groupId;
  public final KafkaTopic topic;
  public final KafkaServer server;
}
