package com.jcaa.notificationservice.adapter.email.mapper;

import com.jcaa.notificationservice.adapter.email.config.BrevoProperties;
import com.jcaa.notificationservice.adapter.email.request.BrevoEmailRequest;
import com.jcaa.notificationservice.domain.core.model.EmailDestination;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public final class BrevoEmailRequestMapper {

  private static final String IDEMPOTENCY_HEADER = "idempotencyKey";

  public BrevoEmailRequest toRequest(
      final EmailDestination destination, final BrevoProperties properties) {
    return new BrevoEmailRequest(
        new BrevoEmailRequest.Contact(properties.fromAddress(), properties.fromName()),
        List.of(
            new BrevoEmailRequest.Contact(
                destination.destinationEmail().value(), destination.destinationName())),
        destination.subject(),
        destination.body(),
        Map.of(IDEMPOTENCY_HEADER, UUID.randomUUID().toString()));
  }
}
