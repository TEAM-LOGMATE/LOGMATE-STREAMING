package com.logmate.streaming.processor.storage.service;

import com.logmate.streaming.common.dto.log.LogEnvelope;

/**
 * LogStorageService
 *
 * 로그 저장소 연동을 위한 표준 인터페이스.
 *
 * 책임
 * - 표준화된 로그 객체(LogEnvelope)를 외부 저장소에 영속화한다.
 * - 저장소는 OpenSearch, RDBMS, NoSQL, 파일 시스템, 클라우드 스토리지(S3, GCS) 등 다양하게 구현할 수 있다.
 *
 * 확장 가이드
 * - 새로운 저장소를 지원하려면 이 인터페이스를 구현한 클래스를 작성하고,
 *   {@link com.logmate.streaming.processor.LogProcessor}의 구현체({@link com.logmate.streaming.processor.storage.LogStorageProcessor})에서 주입받아 사용한다.
 * - 저장 실패 시 예외를 던지면 상위 Processor가 이를 감지하고 에러 처리(onErrorResume, DLQ 전송 등)를 수행한다.
 */
public interface LogStorageService {
  void storeLogEnvelope(LogEnvelope env);
}
