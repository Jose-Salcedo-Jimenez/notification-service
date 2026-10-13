package com.jcaa.notificationservice.adapter.email.config;

import java.time.Duration;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "brevo")
public record BrevoProperties(
    String baseUrl,
    String apiKey,
    String fromAddress,
    String fromName,
    Duration connectTimeout,
    Duration readTimeout,
    int retryMaxAttempts,
    Duration retryInitialDelay,
    double retryMultiplier,
    float circuitFailureThreshold,
    int circuitSlidingWindowSize,
    int circuitMinimumCalls,
    Duration circuitOpenDuration) {

  private static final String REST_API_KEY_PREFIX = "xkeysib-";
  private static final String REQUIRED_PROPERTY_MESSAGE =
      "The required Brevo property '%s' must not be blank.";
  private static final String INVALID_API_KEY_MESSAGE =
      "The Brevo API key must be a REST API key with prefix 'xkeysib-'.";
  private static final String POSITIVE_VALUE_MESSAGE =
      "The Brevo property '%s' must be greater than zero.";

  public BrevoProperties {
    requireText(baseUrl, "base-url");
    requireText(apiKey, "api-key");
    requireText(fromAddress, "from-address");
    requireText(fromName, "from-name");
    requirePositive(connectTimeout, "connect-timeout");
    requirePositive(readTimeout, "read-timeout");
    requirePositive(retryMaxAttempts, "retry-max-attempts");
    requirePositive(retryInitialDelay, "retry-initial-delay");
    requirePositive(retryMultiplier, "retry-multiplier");
    requirePositive(circuitFailureThreshold, "circuit-failure-threshold");
    requirePositive(circuitSlidingWindowSize, "circuit-sliding-window-size");
    requirePositive(circuitMinimumCalls, "circuit-minimum-calls");
    requirePositive(circuitOpenDuration, "circuit-open-duration");
    if (!apiKey.startsWith(REST_API_KEY_PREFIX)) {
      throw new IllegalArgumentException(INVALID_API_KEY_MESSAGE);
    }
  }

  private static void requireText(final String value, final String propertyName) {
    if (Objects.isNull(value) || value.isBlank()) {
      throw new IllegalArgumentException(String.format(REQUIRED_PROPERTY_MESSAGE, propertyName));
    }
  }

  private static void requirePositive(final Duration value, final String propertyName) {
    if (Objects.isNull(value) || value.isZero() || value.isNegative()) {
      throw new IllegalArgumentException(String.format(POSITIVE_VALUE_MESSAGE, propertyName));
    }
  }

  private static void requirePositive(final int value, final String propertyName) {
    if (value <= 0) {
      throw new IllegalArgumentException(String.format(POSITIVE_VALUE_MESSAGE, propertyName));
    }
  }

  private static void requirePositive(final double value, final String propertyName) {
    if (value <= 0) {
      throw new IllegalArgumentException(String.format(POSITIVE_VALUE_MESSAGE, propertyName));
    }
  }
}
