package com.jcaa.notificationservice.adapter.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.notificationservice.adapter.email.config.BrevoProperties;
import com.jcaa.notificationservice.adapter.email.mapper.BrevoEmailRequestMapper;
import com.jcaa.notificationservice.domain.core.exception.EmailSenderException;
import com.jcaa.notificationservice.domain.core.model.EmailDestination;
import com.jcaa.notificationservice.domain.core.vo.UserEmail;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

class BrevoEmailSenderAdapterTest {

  private ExchangeFunction exchangeFunction;
  private EmailDestination destination;

  @BeforeEach
  void setUp() {
    destination =
        new EmailDestination(
            new UserEmail("recipient@example.com"),
            "Recipient",
            "Welcome",
            "<p>Hello</p>");
  }

  @Test
  void shouldWaitForBrevoResponseBeforeReturning() {
    // Arrange
    final AtomicBoolean responseConsumed = new AtomicBoolean();
    final AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
    exchangeFunction =
        request ->
            Mono.fromSupplier(
                () -> {
                  capturedRequest.set(request);
                  responseConsumed.set(true);
                  return response(HttpStatus.ACCEPTED);
                });

    // Act
    assertDoesNotThrow(() -> adapter(defaultProperties()).send(destination));

    // Assert
    assertAll(
        () -> assertThat(responseConsumed).isTrue(),
        () -> assertThat(capturedRequest.get().method()).isEqualTo(HttpMethod.POST),
        () -> assertThat(capturedRequest.get().url().getPath()).isEqualTo("/v3/smtp/email"));
  }

  @Test
  void shouldRetryTransientResponsesAndEventuallySucceed() {
    // Arrange
    final AtomicInteger calls = new AtomicInteger();
    final HttpStatus[] responses = {
      HttpStatus.SERVICE_UNAVAILABLE,
      HttpStatus.REQUEST_TIMEOUT,
      HttpStatus.TOO_MANY_REQUESTS,
      HttpStatus.ACCEPTED
    };
    exchangeFunction =
        _ -> Mono.just(response(responses[calls.getAndIncrement()]));
    final BrevoProperties properties = properties(4, 10);

    // Act
    assertDoesNotThrow(() -> adapter(properties).send(destination));

    // Assert
    assertThat(calls).hasValue(4);
  }

  @Test
  void shouldNotRetryPermanentClientResponse() {
    // Arrange
    final AtomicInteger calls = new AtomicInteger();
    exchangeFunction =
        _ -> {
          calls.incrementAndGet();
          return Mono.just(response(HttpStatus.BAD_REQUEST));
        };

    // Act
    final EmailSenderException exception =
        assertThrows(
            EmailSenderException.class, () -> adapter(defaultProperties()).send(destination));

    // Assert
    assertAll(
        () -> assertThat(exception.getCause()).isNotNull(),
        () -> assertThat(exception.getMessage()).doesNotContain("recipient@example.com"),
        () -> assertThat(calls).hasValue(1));
  }

  @Test
  void shouldRetryConnectionFailure() {
    // Arrange
    final WebClientRequestException connectionFailure =
        new WebClientRequestException(
            new IOException("connection refused"),
            HttpMethod.POST,
            URI.create("https://api.brevo.test/v3/smtp/email"),
            HttpHeaders.EMPTY);
    final AtomicInteger calls = new AtomicInteger();
    exchangeFunction =
        _ ->
            calls.getAndIncrement() == 0
                ? Mono.error(connectionFailure)
                : Mono.just(response(HttpStatus.ACCEPTED));

    // Act
    assertDoesNotThrow(() -> adapter(defaultProperties()).send(destination));

    // Assert
    assertThat(calls).hasValue(2);
  }

  @Test
  void shouldOpenCircuitAfterConfiguredFailures() {
    // Arrange
    final AtomicInteger calls = new AtomicInteger();
    exchangeFunction =
        _ -> {
          calls.incrementAndGet();
          return Mono.just(response(HttpStatus.SERVICE_UNAVAILABLE));
        };
    final BrevoEmailSenderAdapter adapter = adapter(properties(1, 2));

    // Act
    assertThrows(EmailSenderException.class, () -> adapter.send(destination));
    assertThrows(EmailSenderException.class, () -> adapter.send(destination));
    final EmailSenderException openCircuitException =
        assertThrows(EmailSenderException.class, () -> adapter.send(destination));

    // Assert
    assertThat(openCircuitException.getCause())
        .isInstanceOf(io.github.resilience4j.circuitbreaker.CallNotPermittedException.class);
    assertThat(calls).hasValue(2);
  }

  @Test
  void shouldPropagateUnexpectedProgrammingFailure() {
    // Arrange
    final IllegalStateException programmingFailure = new IllegalStateException("unexpected");
    exchangeFunction = _ -> Mono.error(programmingFailure);

    // Act
    final IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> adapter(defaultProperties()).send(destination));

    // Assert
    assertThat(exception).isSameAs(programmingFailure);
  }

  @Test
  void shouldDescribeMissingRetryCauseAsUnknown() {
    // Arrange

    // Act
    final String exceptionType = BrevoEmailSenderAdapter.exceptionType(null);

    // Assert
    assertThat(exceptionType).isEqualTo("unknown");
  }

  private BrevoEmailSenderAdapter adapter(final BrevoProperties properties) {
    final WebClient webClient =
        WebClient.builder().baseUrl(properties.baseUrl()).exchangeFunction(exchangeFunction).build();
    return new BrevoEmailSenderAdapter(properties, new BrevoEmailRequestMapper(), webClient);
  }

  private static ClientResponse response(final HttpStatus status) {
    return ClientResponse.create(status).build();
  }

  private static BrevoProperties defaultProperties() {
    return properties(3, 5);
  }

  private static BrevoProperties properties(final int attempts, final int minimumCalls) {
    return new BrevoProperties(
        "https://api.brevo.test",
        "xkeysib-test-api-key",
        "no-reply@example.com",
        "Notification Service",
        Duration.ofSeconds(2),
        Duration.ofSeconds(5),
        attempts,
        Duration.ofMillis(1),
        2.0,
        50,
        minimumCalls,
        minimumCalls,
        Duration.ofSeconds(30));
  }
}
