package com.logmate.streaming.messaging.producer;

/**
 * DlqProducer
 *
 * Dead Letter Queue(DLQ)로 실패한 로그 메시지를 전송하기 위한 인터페이스.
 * Consumer나 Pipeline에서 처리할 수 없는 메시지가 발생했을 때,
 * 해당 메시지를 DLQ 토픽이나 저장소로 전달하여 유실을 방지한다.
 *
 * 기본 구현체 : KafkaDlqProducer
 */
public interface DlqProducer {
  void send(String key, String message, Throwable cause);
}
