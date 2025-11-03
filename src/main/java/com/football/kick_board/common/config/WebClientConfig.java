package com.football.kick_board.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Value("${football.api.key}") // application.properties에서 주입
  private String apiKey;

  @Value("${football.api.base-url}")
  private String baseUrl;

  @Bean
  public WebClient footballDataWebClient() {
    return WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("X-Auth-Token", apiKey)
        .build();
  }

}
