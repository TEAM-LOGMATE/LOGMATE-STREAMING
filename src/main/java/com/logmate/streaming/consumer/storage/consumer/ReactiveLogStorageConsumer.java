package com.logmate.streaming.consumer.storage.consumer;

import com.logmate.streaming.common.dto.SpringBootParsedLog;
import com.logmate.streaming.consumer.storage.service.LogStorageService;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReactiveLogStorageConsumer {

  private final KafkaReceiver<String, SpringBootParsedLog> kafkaReceiver;
  private final LogStorageService logStorageService;

  @PostConstruct
  public void consume() {
    Executors.newSingleThreadExecutor().submit(() ->
        kafkaReceiver.receive()
            .publishOn(Schedulers.boundedElastic())
            .doOnSubscribe(s -> log.info("Subscribed to KafkaReceiver"))
            .doOnNext(record -> {
              SpringBootParsedLog logData = record.value();
              log.info("Received: {}", logData);

              logStorageService.storeLog(logData);
            })
            .doOnError(e -> log.error("KafkaReceiver error", e))
            .subscribe());
  }
}
