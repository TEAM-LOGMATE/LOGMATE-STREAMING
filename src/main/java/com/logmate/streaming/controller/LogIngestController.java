package com.logmate.streaming.controller;

import com.logmate.streaming.common.constant.log.LogType;
import com.logmate.streaming.common.dto.log.SpringBootParsedLog;
import com.logmate.streaming.common.dto.log.TomcatAccessParsedLog;
import com.logmate.streaming.producer.GenericKafkaLogProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/log-mate/logs")
public class LogIngestController {

  private final GenericKafkaLogProducer genericKafkaLogProducer;

  @PostMapping("/springboot/{agentId}/{thNum}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<Void> receiveSpringLog(@RequestBody Mono<SpringBootParsedLog> logMessageMono,
      @PathVariable String agentId, @PathVariable String thNum) {
    log.info("[LogIngestController] Received SpringBoot log. agentId: {}", agentId);
    return logMessageMono
        .flatMap(log -> genericKafkaLogProducer.sendLog(log, LogType.SPRING_BOOT, agentId, thNum));
  }

  @PostMapping("/tomcat/{agentId}/{thNum}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<Void> receiveTomcatLog(@RequestBody Mono<TomcatAccessParsedLog> logMessageMono,
      @PathVariable String agentId, @PathVariable String thNum) {
    log.info("[LogIngestController] Received TomcatAccess log. agentId: {}", agentId);
    return logMessageMono
        .flatMap(log -> genericKafkaLogProducer.sendLog(log, LogType.TOMCAT_ACCESS, agentId, thNum));
  }
}
