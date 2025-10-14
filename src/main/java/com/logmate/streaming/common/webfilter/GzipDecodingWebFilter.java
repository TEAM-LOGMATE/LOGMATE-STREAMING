package com.logmate.streaming.common.webfilter;

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

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class GzipDecodingWebFilter implements WebFilter {
  private static final List<String> GZIP_CT = List.of(
      "application/gzip",
      "application/x-gzip"
  );

  @Value("${web.gzip.max-bytes:10485760}") // 기본 10MB
  private int maxBytes;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    log.info("[GzipDecodingWebFilter] filter called");
    HttpHeaders headers = exchange.getRequest().getHeaders();
    String contentEncoding = headers.getFirst(HttpHeaders.CONTENT_ENCODING);
    String contentType = headers.getContentType() != null ? headers.getContentType().toString() : null;

    boolean isGzip =
        (contentEncoding != null && "gzip".equalsIgnoreCase(contentEncoding))
            || (contentType != null && GZIP_CT.stream().anyMatch(ct -> ct.equalsIgnoreCase(contentType)));

    if (!isGzip) {
      // gzip 아니면 그대로 진행
      return chain.filter(exchange);
    }

    // 바디를 한 번 모아서(사이즈 체크) gunzip → JSON으로 변경
    return DataBufferUtils.join(exchange.getRequest().getBody())
        .flatMap(joined -> {
          int readable = joined.readableByteCount();
          if (readable > maxBytes) {
            DataBufferUtils.release(joined);
            return Mono.error(new IllegalArgumentException("Compressed body too large"));
          }
          byte[] compressed = new byte[readable];
          joined.read(compressed);
          DataBufferUtils.release(joined);

          byte[] decompressed;
          try {
            decompressed = gunzip(compressed, maxBytes);
          } catch (IOException e) {
            return Mono.error(new IllegalArgumentException("Failed to decompress gzip body", e));
          }

          if (decompressed.length > maxBytes) {
            return Mono.error(new IllegalArgumentException("Decompressed body too large"));
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

          return chain.filter(exchange.mutate().request(decorated).build());
        });
  }

  private byte[] gunzip(byte[] compressed, int limit) throws IOException {
    log.info("[GzipDecodingWebFilter] gunzip called");
    try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
      // 안전한 확장: limit까지 읽기
      byte[] buf = new byte[Math.min(8192, limit)];
      int total = 0;
      byte[] out = new byte[0];
      int r;
      while ((r = gis.read(buf)) != -1) {
        if (total + r > limit) {
          throw new IOException("Decompressed size exceeds limit");
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
