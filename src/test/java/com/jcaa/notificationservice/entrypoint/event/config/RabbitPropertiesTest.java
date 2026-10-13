package com.jcaa.notificationservice.entrypoint.event.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class RabbitPropertiesTest {

  @Test
  void shouldAcceptValidProperties() {
    // Arrange
    final RabbitProperties.Notification notification =
        new RabbitProperties.Notification("notification.events.queue", "user.#");
    final RabbitProperties.Domain domain = new RabbitProperties.Domain("domain.events");

    // Act
    final RabbitProperties properties = new RabbitProperties(notification, domain);

    // Assert
    assertAll(
        () -> assertThat(properties.notification()).isEqualTo(notification),
        () -> assertThat(properties.domain()).isEqualTo(domain));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void shouldRejectMissingQueue(final String queue) {
    // Arrange

    // Act
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new RabbitProperties.Notification(queue, "user.#"));

    // Assert
    assertThat(exception.getMessage())
        .isEqualTo("RabbitMQ notification properties must not be blank.");
  }

  @ParameterizedTest
  @NullAndEmptySource
  void shouldRejectMissingRoutingKey(final String routingKey) {
    // Arrange

    // Act
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new RabbitProperties.Notification("notification.events.queue", routingKey));

    // Assert
    assertThat(exception.getMessage())
        .isEqualTo("RabbitMQ notification properties must not be blank.");
  }

  @ParameterizedTest
  @NullAndEmptySource
  void shouldRejectMissingExchange(final String exchange) {
    // Arrange

    // Act
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> new RabbitProperties.Domain(exchange));

    // Assert
    assertThat(exception.getMessage())
        .isEqualTo("RabbitMQ notification properties must not be blank.");
  }

  @Test
  void shouldRejectNullNestedProperties() {
    // Arrange
    final RabbitProperties.Notification notification =
        new RabbitProperties.Notification("notification.events.queue", "user.#");
    final RabbitProperties.Domain domain = new RabbitProperties.Domain("domain.events");

    // Act
    final NullPointerException missingNotification =
        assertThrows(NullPointerException.class, () -> new RabbitProperties(null, domain));
    final NullPointerException missingDomain =
        assertThrows(NullPointerException.class, () -> new RabbitProperties(notification, null));

    // Assert
    assertThat(missingNotification.getMessage())
        .isEqualTo("RabbitMQ notification properties must not be blank.");
    assertThat(missingDomain.getMessage())
        .isEqualTo("RabbitMQ notification properties must not be blank.");
  }
}
