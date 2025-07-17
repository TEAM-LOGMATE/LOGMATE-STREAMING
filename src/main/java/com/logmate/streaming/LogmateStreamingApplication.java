package com.logmate.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class LogmateStreamingApplication {

  public static void main(String[] args) {
    SpringApplication.run(LogmateStreamingApplication.class, args);
  }

}
