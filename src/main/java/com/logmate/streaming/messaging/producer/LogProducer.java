package com.logmate.streaming.messaging.producer;

import com.logmate.streaming.global.log.LogType;
import com.logmate.streaming.global.log.ParsedLogData;
import reactor.core.publisher.Mono;

/**
 * LogProducer
 *
 * 로그 데이터를 외부 메시지 브로커(Kafka 등)로 전송하기 위한 인터페이스.
 * ParsedLogData, 지정된 로그 타입, agentId, thNum 정보를 포함하여 전송한다.
 *
 * 기본 구현체 : KafkaLogProducer
 */
public interface LogProducer {
  Mono<Void> sendLog(ParsedLogData logData, LogType logType, String agentId, String thNum);
}
