package com.jcaa.notificationservice.adapter.email.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration(proxyBeanMethods = false)
public class BrevoClientConfiguration {

  private static final String API_KEY_HEADER = "api-key";
  private static final String CONNECTION_POOL_NAME = "brevo-email-pool";

  @Bean
  public WebClient brevoWebClient(final BrevoProperties properties) {
    final ConnectionProvider connectionProvider =
        ConnectionProvider.builder(CONNECTION_POOL_NAME)
            .maxIdleTime(properties.readTimeout())
            .build();
    final HttpClient httpClient =
        HttpClient.create(connectionProvider)
            .option(
                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                Math.toIntExact(properties.connectTimeout().toMillis()))
            .responseTimeout(properties.readTimeout())
            .keepAlive(true);
    return WebClient.builder()
        .baseUrl(properties.baseUrl())
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .defaultHeader(API_KEY_HEADER, properties.apiKey())
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
