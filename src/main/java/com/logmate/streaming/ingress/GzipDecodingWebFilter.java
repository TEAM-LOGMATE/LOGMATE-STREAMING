package com.logmate.streaming.ingress;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * GzipDecodingWebFilter
 * <p>
 * 요청 본문이 Gzip 압축되어 있을 경우 자동으로 해제하여 downstream handler가 일반 JSON 요청처럼 처리할 수 있도록 변환하는 WebFilter.
 * <p>
 * Reactive Stream 환경에서는 요청 body가 여러 DataBuffer로 분리되어 들어올 수 있기 때문에 DataBufferUtils.join()으로 전체를 모은 뒤
 * 압축 해제를 수행한다.
 * <p>
 * 압축 해제 후 Content-Encoding 헤더를 제거하고 Content-Type을 application/json으로 지정한다.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class GzipDecodingWebFilter implements WebFilter {

  private static final List<String> GZIP_CT = List.of(
      "application/gzip",
      "application/x-gzip"
  );

  /**
   * 최대 허용 바이트 (기본 10MB)
   */
  @Value("${web.gzip.max-bytes:10485760}")
  private int maxBytes;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    HttpHeaders headers = exchange.getRequest().getHeaders();
    String contentEncoding = headers.getFirst(HttpHeaders.CONTENT_ENCODING);
    String contentType =
        headers.getContentType() != null ? headers.getContentType().toString() : null;

    boolean isGzip =
        (contentEncoding != null && "gzip".equalsIgnoreCase(contentEncoding))
            || (contentType != null && GZIP_CT.stream()
            .anyMatch(ct -> ct.equalsIgnoreCase(contentType)));

    if (!isGzip) {
      // gzip이 아닌 경우 그대로 통과
      return chain.filter(exchange);
    }

    log.info("[GzipDecodingWebFilter] Detected gzip request (path={}, encoding={})",
        exchange.getRequest().getURI(), contentEncoding);
    // 바디를 한 번 모아서(사이즈 체크) gunzip → JSON으로 변경
    return DataBufferUtils.join(exchange.getRequest().getBody())
        .flatMap(joined -> {
          int readable = joined.readableByteCount();
          if (readable > maxBytes) {
            DataBufferUtils.release(joined);
            return Mono.error(new IllegalArgumentException(
                "[GzipDecodingWebFilter] Compressed body too large (" + readable + " bytes)"));
          }
          byte[] compressed = new byte[readable];
          joined.read(compressed);
          DataBufferUtils.release(joined);

          byte[] decompressed;
          try {
            decompressed = gunzip(compressed, maxBytes);
          } catch (IOException e) {
            return Mono.error(new IllegalArgumentException(
                "[GzipDecodingWebFilter] Failed to decompress gzip body", e));
          }

          if (decompressed.length > maxBytes) {
            return Mono.error(new IllegalArgumentException(
                "[GzipDecodingWebFilter] Decompressed body too large (\" + decompressed.length + \" bytes)\")"));
          }

          DataBufferFactory factory = exchange.getResponse().bufferFactory();
          DataBuffer newBody = factory.wrap(decompressed);

          // 요청 데코레이터로 바디/헤더 교체
          ServerHttpRequest decorated = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
              HttpHeaders h = new HttpHeaders();
              h.putAll(super.getHeaders());
              // JSON으로 인식시키기
              h.setContentType(MediaType.APPLICATION_JSON);
              // 압축은 제거
              h.remove(HttpHeaders.CONTENT_ENCODING);
              // 길이 재설정 (모르겠으면 제거)
              h.remove(HttpHeaders.CONTENT_LENGTH);
              return h;
            }

            @Override
            public Flux<DataBuffer> getBody() {
              return Flux.just(DataBufferUtils.retain(newBody));
            }
          };

          log.info("[GzipDecodingWebFilter] Decompressed {} → {} bytes (path={})",
              readable, decompressed.length, exchange.getRequest().getURI());
          return chain.filter(exchange.mutate().request(decorated).build());
        });
  }

  private byte[] gunzip(byte[] compressed, int limit) throws IOException {
    try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
      // 안전한 확장: limit까지 읽기
      byte[] buf = new byte[Math.min(8192, limit)];
      int total = 0;
      byte[] out = new byte[0];
      int r;
      while ((r = gis.read(buf)) != -1) {
        if (total + r > limit) {
          throw new IOException("[GzipDecodingWebFilter] Decompressed size exceeds limit");
        }
        byte[] newOut = new byte[total + r];
        System.arraycopy(out, 0, newOut, 0, total);
        System.arraycopy(buf, 0, newOut, total, r);
        out = newOut;
        total += r;
      }
      return out;
    }
  }
}
