package com.logmate.streaming.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {
  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();

    // 프론트엔드 Origin 허용 (필요 시 여러 개 추가 가능)
    config.addAllowedOriginPattern("http://localhost:4173"); // 또는 "https://your-frontend.com"
    config.addAllowedOriginPattern("http://localhost:3000"); // 또는 "https://your-frontend.com"


    // 헤더, 메서드 허용
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");

    // 쿠키 및 인증정보 허용
    config.setAllowCredentials(true);

    // 캐시 시간 (옵션)
    config.setMaxAge(3600L);

    // URL 매핑
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return new CorsFilter(source);
  }
}
