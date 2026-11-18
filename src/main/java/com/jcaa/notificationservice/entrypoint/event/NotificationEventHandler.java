package com.jcaa.notificationservice.entrypoint.event;

import com.jcaa.notificationservice.application.exception.InvalidNotificationEventException;
import com.jcaa.notificationservice.application.port.in.ProcessNotificationEventUseCase;
import com.jcaa.notificationservice.entrypoint.event.mapper.DomainEventRequestMapper;
import com.jcaa.notificationservice.entrypoint.event.request.DomainEventRequest;
import java.util.Objects;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public final class NotificationEventHandler {

  private static final String INVALID_MESSAGE = "The notification event could not be parsed.";

  private final ProcessNotificationEventUseCase processNotificationEventUseCase;
  private final DomainEventRequestMapper requestMapper;
  private final JsonMapper jsonMapper;

  public NotificationEventHandler(
      final ProcessNotificationEventUseCase processNotificationEventUseCase,
      final DomainEventRequestMapper requestMapper,
      final JsonMapper jsonMapper) {
    this.processNotificationEventUseCase =
        Objects.requireNonNull(processNotificationEventUseCase);
    this.requestMapper = Objects.requireNonNull(requestMapper);
    this.jsonMapper = Objects.requireNonNull(jsonMapper);
  }

  @KafkaListener(
      topics = {
        "${kafka.topic.user-events:user-events}",
        "${kafka.topic.account-events:account-events}"
      },
      groupId = "${KAFKA_GROUP_ID:notification-service}")
  public void handle(final String message) {
    processNotificationEventUseCase.process(requestMapper.toCommand(parse(message)));
  }

  private DomainEventRequest parse(final String message) {
    if (Objects.isNull(message)) {
      throw new InvalidNotificationEventException(INVALID_MESSAGE);
    }
    try {
      return jsonMapper.readValue(message, DomainEventRequest.class);
    } catch (final JacksonException exception) {
      throw new InvalidNotificationEventException(INVALID_MESSAGE);
    }
  }
}
