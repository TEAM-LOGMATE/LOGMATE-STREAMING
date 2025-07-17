package com.logmate.streaming.consumer.config;

import com.logmate.streaming.common.constant.kafka.KafkaConstant;
import com.logmate.streaming.common.dto.SpringBootParsedLog;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReactiveKafkaConsumerConfig {

  private final KafkaConstant kafkaConstant;

  @Bean
  public ReceiverOptions<String, SpringBootParsedLog> receiverOptions() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConstant.server.URL);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "logmate-group-" + UUID.randomUUID());
    props.put(ConsumerConfig.CLIENT_ID_CONFIG, "logmate-client-" + UUID.randomUUID());
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // 필수!
    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, SpringBootParsedLog.class.getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    return ReceiverOptions.<String, SpringBootParsedLog>create(props)
        .subscription(Collections.singleton(kafkaConstant.topic.LOG_TOPIC))
        .addAssignListener(partitions -> log.info("✅ Assigned partitions: {}", partitions))
        .addRevokeListener(partitions -> log.info("❌ Revoked partitions: {}", partitions));
  }

  @Bean
  public KafkaReceiver<String, SpringBootParsedLog> kafkaReceiver(ReceiverOptions<String, SpringBootParsedLog> options) {
    return KafkaReceiver.create(options);
  }
}
