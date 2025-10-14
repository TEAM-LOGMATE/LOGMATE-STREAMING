package com.logmate.streaming.messaging.consumer.config;

import com.logmate.streaming.global.constant.kafka.KafkaConstant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReactiveKafkaConsumerConfig {

  private final KafkaConstant kafkaConstant;

  @Bean
  public ReceiverOptions<String, String> receiverOptions() {
    log.info("Creating Kafka receiver options {}", kafkaConstant.server.bootstrapServers);
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConstant.server.bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConstant.groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    return ReceiverOptions.<String, String>create(props)
        .subscription(Collections.singleton(kafkaConstant.topic.LOG_TOPIC))
        .addAssignListener(partitions -> log.info("Assigned partitions: {}", partitions))
        .addRevokeListener(partitions -> log.info("Revoked partitions: {}", partitions));
  }

  @Bean
  public KafkaReceiver<String, String> kafkaReceiver(ReceiverOptions<String, String> options) {
    return KafkaReceiver.create(options);
  }
}
