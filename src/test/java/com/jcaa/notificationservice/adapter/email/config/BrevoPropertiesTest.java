package com.jcaa.notificationservice.adapter.email.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class BrevoPropertiesTest {

  @Test
  void shouldAcceptValidProperties() {
    // Arrange

    // Act

    // Assert
    assertDoesNotThrow(() -> properties("https://api.brevo.test", "xkeysib-valid"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void shouldRejectMissingTextProperty(final String baseUrl) {
    // Arrange

    // Act
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> properties(baseUrl, "xkeysib-valid"));

    // Assert
    assertThat(exception.getMessage()).contains("base-url");
  }

  @Test
  void shouldRejectNonRestApiKey() {
    // Arrange
    final String smtpKey = "xsmtpsib-invalid";

    // Act
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> properties("https://api.brevo.test", smtpKey));

    // Assert
    assertThat(exception.getMessage()).contains("xkeysib-");
  }

  @Test
  void shouldRejectNullDuration() {
    // Arrange

    // Act
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> createPropertiesWithConnectTimeout(null));

    // Assert
    assertThat(exception.getMessage()).contains("connect-timeout");
  }

  @Test
  void shouldRejectZeroDuration() {
    // Arrange

    // Act
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> createPropertiesWithConnectTimeout(Duration.ZERO));

    // Assert
    assertThat(exception.getMessage()).contains("connect-timeout");
  }

  @Test
  void shouldRejectNegativeDuration() {
    // Arrange

    // Act
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> createPropertiesWithConnectTimeout(Duration.ofSeconds(-1)));

    // Assert
    assertThat(exception.getMessage()).contains("connect-timeout");
  }

  @Test
  void shouldRejectNonPositiveInteger() {
    // Arrange

    // Act
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            BrevoPropertiesTest::createPropertiesWithNonPositiveRetryAttempts);

    // Assert
    assertThat(exception.getMessage()).contains("retry-max-attempts");
  }

  @Test
  void shouldRejectNonPositiveDecimal() {
    // Arrange

    // Act
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            BrevoPropertiesTest::createPropertiesWithNonPositiveRetryMultiplier);

    // Assert
    assertThat(exception.getMessage()).contains("retry-multiplier");
  }

  private static BrevoProperties properties(final String baseUrl, final String apiKey) {
    return new BrevoProperties(
        baseUrl,
        apiKey,
        "no-reply@example.com",
        "Notification Service",
        Duration.ofSeconds(2),
        Duration.ofSeconds(5),
        3,
        Duration.ofMillis(10),
        2.0,
        50,
        10,
        5,
        Duration.ofSeconds(30));
  }

  private static void createPropertiesWithConnectTimeout(final Duration connectTimeout) {
    final BrevoProperties properties =
        new BrevoProperties(
            "https://api.brevo.test",
            "xkeysib-valid",
            "no-reply@example.com",
            "Notification Service",
            connectTimeout,
            Duration.ofSeconds(5),
            3,
            Duration.ofMillis(10),
            2.0,
            50,
            10,
            5,
            Duration.ofSeconds(30));
    assertThat(properties.apiKey()).isEqualTo("xkeysib-valid");
  }

  private static void createPropertiesWithNonPositiveRetryAttempts() {
    final BrevoProperties properties =
        new BrevoProperties(
            "https://api.brevo.test",
            "xkeysib-valid",
            "no-reply@example.com",
            "Notification Service",
            Duration.ofSeconds(2),
            Duration.ofSeconds(5),
            0,
            Duration.ofMillis(10),
            2.0,
            50,
            10,
            5,
            Duration.ofSeconds(30));
    assertThat(properties.apiKey()).isEqualTo("xkeysib-valid");
  }

  private static void createPropertiesWithNonPositiveRetryMultiplier() {
    final BrevoProperties properties =
        new BrevoProperties(
            "https://api.brevo.test",
            "xkeysib-valid",
            "no-reply@example.com",
            "Notification Service",
            Duration.ofSeconds(2),
            Duration.ofSeconds(5),
            3,
            Duration.ofMillis(10),
            0,
            50,
            10,
            5,
            Duration.ofSeconds(30));
    assertThat(properties.apiKey()).isEqualTo("xkeysib-valid");
  }
}
