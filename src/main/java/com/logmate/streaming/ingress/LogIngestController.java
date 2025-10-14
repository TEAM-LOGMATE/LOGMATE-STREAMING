package com.logmate.streaming.ingress;

import com.logmate.streaming.ingress.dto.SpringBootParsedLogStreamReq;
import com.logmate.streaming.ingress.dto.TomcatAccessParsedLogStreamReq;
import com.logmate.streaming.global.log.LogType;
import com.logmate.streaming.global.log.ParsedLogData;
import com.logmate.streaming.global.log.SpringBootParsedLog;
import com.logmate.streaming.global.log.TomcatAccessParsedLog;
import com.logmate.streaming.global.util.ParsedLogMapper;
import com.logmate.streaming.ingress.validator.LogStreamRequestValidator;
import com.logmate.streaming.messaging.producer.LogProducer;
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
   * 확장 방법: * - 새로운 로그 타입을 수신할 수 있도록 추가하려면 LogType enum과 log data를 정의하고, *   아래 @PostMapping 메서드를 새로
   * 정의하여 이 메소드를 호출하시오.
   */
  private Mono<Void> receiveLogs(Flux<? extends ParsedLogData> logFlux, LogType logType,
      String agentId, String thNum) {
    return logFlux
        .doOnSubscribe(s -> log.debug("[Ingress] Start processing {} logs (agentId={}, thNum={})", logType, agentId, thNum))
        .flatMap(parsedLog ->
            logProducer.sendLog(parsedLog, logType, agentId, thNum)
                .onErrorResume(e -> {
                  log.error("[Ingress] Failed to send {} log (agentId={}, thNum={}): {}", logType, agentId, thNum, e.getMessage(), e);
                  return Mono.empty();
                }), 40)
        .doOnComplete(() ->
            log.info("[Ingress] Completed {} log stream (agentId={}, thNum={})", logType, agentId, thNum))
        .then();
  }

  /**
   * 로그 타입 별 HTTP Request 수신 메소드 GzipDecodingWebFilter 를 통해 Gzip 요청일경우 자동으로 파싱됨
   */
  @PostMapping("/springboot/{agentId}/{thNum}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<Void> receiveSpringLogs(
      @RequestBody Mono<List<SpringBootParsedLogStreamReq>> logMessageMono,
      @PathVariable String agentId,
      @PathVariable String thNum
  ) {
    // validation
    LogStreamRequestValidator.validate(agentId, thNum);
    // mapping Mono<List<dto>> -> Flux<parsedLogData>
    Flux<SpringBootParsedLog> parsedLogMono = logMessageMono.map(dtoList -> {
      log.info("[Ingress] Received {} {} logs from agentId={} (thNum={})", dtoList.size(), LogType.SPRING_BOOT, agentId, thNum);
      return dtoList.stream()
          .map(ParsedLogMapper::toEntity)
          .toList();
    }).flatMapMany(Flux::fromIterable);
    return receiveLogs(parsedLogMono, LogType.SPRING_BOOT, agentId, thNum);
  }

  @PostMapping("/tomcat/{agentId}/{thNum}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<Void> receiveTomcatLogs(
      @RequestBody Mono<List<TomcatAccessParsedLogStreamReq>> logMessageMono,
      @PathVariable String agentId,
      @PathVariable String thNum
  ) {
    // validation
    LogStreamRequestValidator.validate(agentId, thNum);
    // mapping Mono<List<dto>> -> Flux<parsedLogData>
    Flux<TomcatAccessParsedLog> parsedLogMono = logMessageMono.map(dtoList -> {
      log.info("[Ingress] Received {} {} logs from agentId={} (thNum={})", dtoList.size(), LogType.TOMCAT_ACCESS, agentId, thNum);
      return dtoList.stream()
          .map(ParsedLogMapper::toEntity)
          .toList();
    }).flatMapMany(Flux::fromIterable);
    return receiveLogs(parsedLogMono, LogType.TOMCAT_ACCESS, agentId, thNum);
  }
}