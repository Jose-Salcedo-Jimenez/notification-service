package com.jcaa.notificationservice.entrypoint.event.config;

import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "rabbitmq")
public record RabbitProperties(Notification notification, Domain domain) {

  private static final String INVALID_PROPERTIES_MESSAGE =
      "RabbitMQ notification properties must not be blank.";

  public RabbitProperties {
    Objects.requireNonNull(notification, INVALID_PROPERTIES_MESSAGE);
    Objects.requireNonNull(domain, INVALID_PROPERTIES_MESSAGE);
  }

  public record Notification(String queue, String routingKey) {

    public Notification {
      requireText(queue);
      requireText(routingKey);
    }
  }

  public record Domain(String exchange) {

    public Domain {
      requireText(exchange);
    }
  }

  private static void requireText(final String value) {
    if (!StringUtils.hasText(value)) {
      throw new IllegalArgumentException(INVALID_PROPERTIES_MESSAGE);
    }
  }
}
