package com.jcaa.notificationservice.adapter.email;

import com.jcaa.notificationservice.adapter.email.config.BrevoProperties;
import com.jcaa.notificationservice.adapter.email.mapper.BrevoEmailRequestMapper;
import com.jcaa.notificationservice.adapter.email.request.BrevoEmailRequest;
import com.jcaa.notificationservice.domain.core.exception.EmailSenderException;
import com.jcaa.notificationservice.domain.core.model.EmailDestination;
import com.jcaa.notificationservice.domain.port.out.EmailSenderPort;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.util.Objects;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public final class BrevoEmailSenderAdapter implements EmailSenderPort {

  private static final Logger log = LoggerFactory.getLogger(BrevoEmailSenderAdapter.class);
  private static final String SEND_EMAIL_PATH = "/v3/smtp/email";
  private static final int REQUEST_TIMEOUT_STATUS = 408;
  private static final int TOO_MANY_REQUESTS_STATUS = 429;
  private static final String RETRY_NAME = "brevo-email-retry";
  private static final String CIRCUIT_BREAKER_NAME = "brevo-email-circuit-breaker";
  private static final String RETRY_LOG =
      "Retrying Brevo email delivery after transient failure. attempt={}, cause={}";
  private static final String DELIVERY_SUCCEEDED_LOG =
      "Email notification accepted by Brevo";
  private static final String DELIVERY_FAILED_LOG =
      "Email notification delivery through Brevo failed. cause={}";

  private final BrevoProperties properties;
  private final BrevoEmailRequestMapper mapper;
  private final WebClient webClient;
  private final Retry retry;
  private final CircuitBreaker circuitBreaker;

  public BrevoEmailSenderAdapter(
      final BrevoProperties properties,
      final BrevoEmailRequestMapper mapper,
      final WebClient webClient) {
    this.properties = properties;
    this.mapper = mapper;
    this.webClient = webClient;
    retry = buildRetry(properties);
    circuitBreaker = buildCircuitBreaker(properties);
    retry
        .getEventPublisher()
        .onRetry(
            event ->
                log.warn(
                    RETRY_LOG,
                    event.getNumberOfRetryAttempts(),
                    exceptionType(event.getLastThrowable())));
  }

  @Override
  public void send(final EmailDestination destination) {
    final BrevoEmailRequest request = mapper.toRequest(destination, properties);
    final Runnable protectedRequest =
        Retry.decorateRunnable(
            retry, CircuitBreaker.decorateRunnable(circuitBreaker, () -> sendRequest(request)));
    try {
      protectedRequest.run();
      log.info(DELIVERY_SUCCEEDED_LOG);
    } catch (final WebClientResponseException
        | WebClientRequestException
        | CallNotPermittedException exception) {
      log.error(DELIVERY_FAILED_LOG, exceptionType(exception));
      throw EmailSenderException.becauseDeliveryFailed(exception);
    }
  }

  private void sendRequest(final BrevoEmailRequest request) {
    webClient.post().uri(SEND_EMAIL_PATH).bodyValue(request).retrieve().toBodilessEntity().block();
  }

  private static Retry buildRetry(final BrevoProperties properties) {
    final IntervalFunction backoff =
        IntervalFunction.ofExponentialBackoff(
            properties.retryInitialDelay().toMillis(), properties.retryMultiplier());
    final RetryConfig retryConfig =
        RetryConfig.custom()
            .maxAttempts(properties.retryMaxAttempts())
            .intervalFunction(backoff)
            .retryOnException(BrevoEmailSenderAdapter::isTransientFailure)
            .build();
    return Retry.of(RETRY_NAME, retryConfig);
  }

  private static CircuitBreaker buildCircuitBreaker(final BrevoProperties properties) {
    final Predicate<Throwable> transientFailure = BrevoEmailSenderAdapter::isTransientFailure;
    final CircuitBreakerConfig circuitBreakerConfig =
        CircuitBreakerConfig.custom()
            .failureRateThreshold(properties.circuitFailureThreshold())
            .slidingWindowSize(properties.circuitSlidingWindowSize())
            .minimumNumberOfCalls(properties.circuitMinimumCalls())
            .waitDurationInOpenState(properties.circuitOpenDuration())
            .recordException(transientFailure)
            .build();
    return CircuitBreaker.of(CIRCUIT_BREAKER_NAME, circuitBreakerConfig);
  }

  private static boolean isTransientFailure(final Throwable exception) {
    if (exception instanceof WebClientRequestException) {
      return true;
    }
    if (exception instanceof WebClientResponseException responseException) {
      final HttpStatusCode status = responseException.getStatusCode();
      return status.is5xxServerError()
          || status.value() == REQUEST_TIMEOUT_STATUS
          || status.value() == TOO_MANY_REQUESTS_STATUS;
    }
    return false;
  }

  static String exceptionType(final Throwable exception) {
    return Objects.isNull(exception) ? "unknown" : exception.getClass().getSimpleName();
  }
}
