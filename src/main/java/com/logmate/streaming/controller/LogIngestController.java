package com.logmate.streaming.controller;

import com.logmate.streaming.common.dto.SpringBootParsedLog;
import com.logmate.streaming.producer.log.producer.KafkaLogProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/log-mate/logs")
public class LogIngestController {

  private final KafkaLogProducer kafkaLogProducer;

  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  public Mono<Void> receiveLog(@RequestBody Mono<SpringBootParsedLog> logMessageMono) {
    return logMessageMono
        .flatMap(kafkaLogProducer::sendLog);
  }
}
