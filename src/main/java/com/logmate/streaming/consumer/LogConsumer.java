package com.logmate.streaming.consumer;

/**
 * LogConsumer
 *
 * 로그 메시지를 소비하기 위한 인터페이스.
 *
 * Kafka, RabbitMQ, 파일 스트림 등 다양한 소스에서 전달되는
 * 로그 메시지를 수신하고 처리하는 동작을 정의한다.
 * 구현체는 메시지를 파싱하고 파이프라인에 전달하며,
 * 실패 시 DLQ 전송이나 알림 등 에러 처리 전략을 적용할 수 있다.
 *
 * 기본 구현체 : UnifiedLogConsumer
 */
public interface LogConsumer {
  void consume(String message);
}
