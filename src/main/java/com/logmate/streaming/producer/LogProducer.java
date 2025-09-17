package com.logmate.streaming.producer;

import com.logmate.streaming.common.log.LogType;
import reactor.core.publisher.Mono;

/**
 * LogProducer
 *
 * 로그 데이터를 외부 메시지 브로커(Kafka 등)로 전송하기 위한 인터페이스.
 * 표준화된 LogEnvelope 형태로 직렬화한 뒤, 지정된 로그 타입과 agent 정보를 포함하여 전송한다.
 *
 * 비동기 전송을 지원하며 Reactor Mono를 반환하여 리액티브 파이프라인과 자연스럽게 연결된다.
 *
 * 기본 구현체 : KafkaLogProducer
 */
public interface LogProducer {
  Mono<Void> sendLog(Object logData, LogType logType, String agentId, String thNum);
}
