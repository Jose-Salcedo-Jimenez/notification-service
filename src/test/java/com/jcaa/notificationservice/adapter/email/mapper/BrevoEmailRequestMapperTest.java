package com.jcaa.notificationservice.adapter.email.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.jcaa.notificationservice.adapter.email.config.BrevoProperties;
import com.jcaa.notificationservice.adapter.email.request.BrevoEmailRequest;
import com.jcaa.notificationservice.domain.core.model.EmailDestination;
import com.jcaa.notificationservice.domain.core.vo.UserEmail;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class BrevoEmailRequestMapperTest {

  private static final String IDEMPOTENCY_HEADER = "idempotencyKey";

  @Test
  void shouldMapDomainModelToBrevoRequest() {
    // Arrange
    final BrevoEmailRequestMapper mapper = new BrevoEmailRequestMapper();
    final BrevoProperties properties = properties();
    final EmailDestination destination =
        new EmailDestination(
            new UserEmail("recipient@example.com"),
            "Recipient",
            "Welcome",
            "<p>Hello</p>");

    // Act
    final BrevoEmailRequest request = mapper.toRequest(destination, properties);

    // Assert
    assertAll(
        () -> assertThat(request.sender().email()).isEqualTo(properties.fromAddress()),
        () -> assertThat(request.sender().name()).isEqualTo(properties.fromName()),
        () -> assertThat(request.to()).singleElement().extracting(BrevoEmailRequest.Contact::email)
            .isEqualTo(destination.destinationEmail().value()),
        () -> assertThat(request.to()).singleElement().extracting(BrevoEmailRequest.Contact::name)
            .isEqualTo(destination.destinationName()),
        () -> assertThat(request.subject()).isEqualTo(destination.subject()),
        () -> assertThat(request.htmlContent()).isEqualTo(destination.body()),
        () -> assertThat(request.headers()).containsOnlyKeys(IDEMPOTENCY_HEADER),
        () -> assertThat(request.headers().get(IDEMPOTENCY_HEADER)).isNotBlank());
  }

  private static BrevoProperties properties() {
    return new BrevoProperties(
        "https://api.brevo.test",
        "xkeysib-test-api-key",
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
}
