package com.logmate.streaming.global.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@ConfigurationProperties(prefix = "web")
@Data
public class WebConfig implements WebFluxConfigurer {

  private List<String> allowedOrigins;
  private List<String> allowedMethods;

  @Override
  public void addCorsMappings(CorsRegistry registry) {

    registry.addMapping("/api/**")
        .allowedOrigins(allowedOrigins.toArray(new String[0]))
        .allowedMethods(allowedMethods.toArray(new String[0]))
        .allowedHeaders("*")
        .allowCredentials(true);

  }
}
