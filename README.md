# 🧩 LogMate Streaming Server
![Java](https://img.shields.io/badge/java-17%2B-blue.svg)
![License](https://img.shields.io/badge/license-Apache--2.0-green.svg)
![Build](https://img.shields.io/badge/build-Gradle-success.svg)

![WebFlux](https://img.shields.io/badge/Spring%20WebFlux-reactive--stack-brightgreen.svg)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-event%20streaming-231f20.svg)
![OpenSearch](https://img.shields.io/badge/OpenSearch-log%20storage-blue.svg)
![WebSocket](https://img.shields.io/badge/WebSocket-real--time%20streaming-yellow.svg)



> 실시간 로그 인입 → AI 처리 요청 → (WebSocket/Storage) Fan-out 을 담당하는 LogMate의 중앙 스트리밍 서버

**Streaming Server**는 Agent에서 전송되는 로그를 수신해 Kafka에 적재하고, AI 서버에 처리 요청 후 결과를 WebSocket(Browser)과 OpenSearch(Storage)로 비동기 병렬 전송합니다. WebFlux 기반 논블로킹 파이프라인으로 높은 동시성과 낮은 지연을 제공합니다.

---
## 🧰 주요 기능
### 인입 & 표준화
| 기능 분류 | 기능 설명 |
|----------|-----------|
| HTTP Ingest API | - 로그 타입별 인입 엔드포인트 지원 (SpringBootParsedLog, TomcatAccessParsedLog)|
| Envelope 표준화 | - { log, logType, agentId, thNum } 형태로 공통 래핑 후 Kafka에 적재 |

### 파이프라인 처리
| 기능 분류 | 기능 설명 |
|----------|-----------|
| 로그 AI 분석 요청 | - AI 서버에 분석 요청 (Tomcat Access Log 만 지원) |
| 로그 저장 | - OpenSearch에 인덱스 기반 저장. Agent 단위로 검색 용이 |
| 로그 스트리밍 | - (agentId, thNum) 단위로 브라우저 WebSocket 푸시 |
| 비동기 병렬 처리 | - WebSocket Broadcast 및 OpenSearch Storage는 병렬 처리 |

---
## 🧱 클래스 다이어그램

<img width="1610" height="1537" alt="logmate drawio" src="https://github.com/user-attachments/assets/9413cd7d-820b-447d-9f7e-c4d79fa1e744" />


처리 순서 규칙: AI → (WS ∥ Storage).
WS/Storage 사이의 순서는 보장하지 않습니다(완전 독립 비동기).

---
## 📦 패키지 구조
```
com/logmate/streaming
├─ common   # 시스템 공통 설정
│  ├─ config    # 시스템 공통 설정 클래스
│  ├─ constant  # 패키지 이름에 관련된 상수 클래스
│  │  ├─ ai
│  │  ├─ kafka
│  │  ├─ log
│  │  └─ opensearch
│  ├─ dto
│  │  └─ log    # 로그 데이터를 표현하는 Data 클래스 (ex. SpringBootParsedLog)
│  └─ util  # 공통 유틸리티 클래스
│
├─ consumer     # Kafka Consumer 설정 및 로직
│  └─ config    # Kafka Consumer 관련 설정
│
├─ controller   # 외부에서 접근 가능한 REST API 진입점
│  
├─ processor    # 로그 처리 전반의 핵심 로직 계층.
│  ├─ ai    
│  │  ├─ config # AI 서버 통신 관련 설정 (WebClient)
│  │  ├─ dto    # AI 서버로 주고받는 요청/응답 DTO
│  │  └─ processor  # 로그 분석 결과를 처리하고 AI 서버에 요청하는 로직
│  ├─ storage
│  │  ├─ config # 로그 저장소 관련 설정
│  │  ├─ processor  # 로그 저장 처리 로직
│  │  └─ service    # 로그 저장에 대한 실제 로직 담당
│  ├─ ws
│  │  ├─ config # WebSocket 통신 설정
│  │  ├─ handler    # 실제 WebSocket 메시지를 주고받는 핸들러
│  │  └─ processor  # WebSocket 처리 로직
│  └─ LogProcessingPipeline # 파이프라인 처리 흐름 오케스트레이션 클래스
│
├─ producer # Kafka Producer 로직
│  
└─ LogmateStreamingApplication  # SpringBoot 애플리케이션 진입점
```
---
## 📡 API
 Wiki 참조
---
## 🛠 빌드 & 실행

---
### 📄 오픈소스 라이선스
| 라이브러리                                                         | 설명                                   | 라이선스                                       |
| ------------------------------------------------------------- | ------------------------------------ | ------------------------------------------ |
| **Spring Boot WebFlux**<br>`spring-boot-starter-webflux`      | 비동기 논블로킹 서버 구축을 위한 WebFlux 프레임워크     | Apache License 2.0                         |
| **Spring for Apache Kafka**<br>`spring-kafka`                 | Kafka 연동을 위한 Spring 통합 라이브러리         | Apache License 2.0                         |
| **Reactor Kafka**<br>`reactor-kafka:1.3.22`                   | Reactive Kafka 클라이언트                 | Apache License 2.0                         |
| **OpenSearch REST Client**<br>`opensearch-rest-client:2.11.0` | OpenSearch REST 클라이언트                | Apache License 2.0                         |
| **OpenSearch Java Client**<br>`opensearch-java:2.11.0`        | OpenSearch Java API 클라이언트            | Apache License 2.0                         |
| **Jackson Databind**<br>`jackson-databind`                    | JSON 직렬화/역직렬화를 위한 핵심 라이브러리           | Apache License 2.0                         |
| **Jackson JSR310**<br>`jackson-datatype-jsr310`               | `LocalDateTime` 등 Java 8 Time API 지원 | Apache License 2.0                         |
| **Apache HttpCore**<br>`httpcore:4.4.14`                      | HTTP 프로토콜 구현을 위한 낮은 수준의 컴포넌트         | Apache License 2.0                         |
| **Apache HttpClient**<br>`httpclient:4.5.13`                  | 고수준 HTTP 클라이언트 구현                    | Apache License 2.0                         |
| **Lombok**<br>`lombok`                                        | 코드 생성을 자동화해주는 Java annotation 라이브러리  | MIT License                                |

---
### 📄 라이선스
본 프로젝트는 **Apache License 2.0** 에 따라 라이선스가 부여됩니다.
자세한 내용은 [LICENSE](./LICENSE) 파일을 참조하세요.

---
### 🙏 기여 가이드
- PR 생성은 [pull_request_template.md](.github/pull_request_template.md) 문서를 참고해 주세요.
- Issue 생성은 [issue_report.md](.github/ISSUE_TEMPLATE/issue_report.md) 문서를 참고해 주세요.

---

### 📲 연락처
email: kan0202@naver.com

---
