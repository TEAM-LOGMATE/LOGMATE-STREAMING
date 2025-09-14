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
@RequestMapping("/api/v1/streaming/logs")
public class LogIngestController {

  private final GenericKafkaLogProducer genericKafkaLogProducer;

  /**
   * 공통 로그 수신 처리 메소드
   */
  private <T> Mono<Void> receiveLog(Mono<T> logMono, LogType logType, String agentId, String thNum) {
    log.info("[LogIngestController] Received {} log. agentId: {}, thNum: {}", logType, agentId, thNum);

    return logMono
        .flatMap(log -> genericKafkaLogProducer.sendLog(log, logType, agentId, thNum))
        .doOnError(e -> log.error(
            "[LogIngestController] Failed to process {} log. agentId: {}, thNum: {}, error: {}",
            logType, agentId, thNum, e.getMessage(), e
        ));
  }

  /**
   * 로그 타입 별 HTTP Request 수신 메소드
   */
  @PostMapping("/springboot/{agentId}/{thNum}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<Void> receiveSpringLog(
      @RequestBody Mono<SpringBootParsedLog> logMessageMono,
      @PathVariable String agentId,
      @PathVariable String thNum
  ) {
    return receiveLog(logMessageMono, LogType.SPRING_BOOT, agentId, thNum);
  }

  @PostMapping("/tomcat/{agentId}/{thNum}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<Void> receiveTomcatLog(
      @RequestBody Mono<TomcatAccessParsedLog> logMessageMono,
      @PathVariable String agentId,
      @PathVariable String thNum
  ) {
    return receiveLog(logMessageMono, LogType.TOMCAT_ACCESS, agentId, thNum);
  }
}

// TODO: agentId 유효성 검증 필요
//   - 빈 문자열 / null 체크
//   - 정의된 형식(UUID, 숫자, 특정 prefix 등) 여부 확인
//   - 존재하지 않는 agentId일 경우 400 Bad Request 반환

// TODO: thNum 유효성 검증 필요
//   - 빈 문자열 / null 체크
//   - 정수인지 여부 검증 (숫자가 아닐 경우 예외 처리)
//   - 허용된 범위(thNum >= 0 등) 확인