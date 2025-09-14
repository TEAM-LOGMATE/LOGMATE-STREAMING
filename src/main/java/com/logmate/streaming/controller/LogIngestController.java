package com.logmate.streaming.controller;

import com.logmate.streaming.common.constant.log.LogType;
import com.logmate.streaming.common.dto.log.SpringBootParsedLog;
import com.logmate.streaming.common.dto.log.TomcatAccessParsedLog;
import com.logmate.streaming.producer.LogProducer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/streaming/logs")
public class LogIngestController {

  private final LogProducer logProducer;

  /**
   * 공통 로그 수신 처리 메소드
   *
   * 확장 방법:
   *    * - 새로운 로그 타입을 수신할 수 있도록 추가하려면 LogType enum과 DTO를 정의하고,
   *    *   아래 @PostMapping 메서드를 새로 만들어 이 메소드를 호출하면 된다.
   */
  private <T> Mono<Void> receiveLogs(Mono<List<T>> logMono, LogType logType, String agentId, String thNum) {
    log.info("[LogIngestController] Received {} logs. agentId: {}, thNum: {}", logType, agentId, thNum);

    return logMono
        .flatMapMany(Flux::fromIterable)
        .flatMap(log -> logProducer.sendLog(log, logType, agentId, thNum), 40)
        .then() // 모든 로그 전송 완료 후 Mono<Void> 반환
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
  public Mono<Void> receiveSpringLogs(
      @RequestBody Mono<List<SpringBootParsedLog>> logMessageMono,
      @PathVariable String agentId,
      @PathVariable String thNum
  ) {
    return receiveLogs(logMessageMono, LogType.SPRING_BOOT, agentId, thNum);
  }

  @PostMapping("/tomcat/{agentId}/{thNum}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<Void> receiveTomcatLogs(
      @RequestBody Mono<List<TomcatAccessParsedLog>> logMessageMono,
      @PathVariable String agentId,
      @PathVariable String thNum
  ) {
    return receiveLogs(logMessageMono, LogType.TOMCAT_ACCESS, agentId, thNum);
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