package com.logmate.streaming.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebConfig implements WebFluxConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {

    registry.addMapping("/api/**")
        .allowedOrigins("http://localhost:4173", "http://localhost:3000")
        .allowedMethods("PUT", "DELETE","GET","POST", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);

  }
}
