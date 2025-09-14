package com.logmate.streaming.common.constant.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaServer {

  @Value("${kafka.server.host}")
  public String HOST;

  @Value("${kafka.server.port}")
  public String PORT;

  @Value("${spring.kafka.bootstrap-servers}")
  public String bootstrapServers;
}
