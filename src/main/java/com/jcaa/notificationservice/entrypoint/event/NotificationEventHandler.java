package com.jcaa.notificationservice.entrypoint.event;

import com.jcaa.notificationservice.application.port.in.ProcessNotificationEventUseCase;
import com.jcaa.notificationservice.entrypoint.event.mapper.DomainEventRequestMapper;
import com.jcaa.notificationservice.entrypoint.event.request.DomainEventRequest;
import java.util.Objects;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public final class NotificationEventHandler {

  private final ProcessNotificationEventUseCase processNotificationEventUseCase;
  private final DomainEventRequestMapper requestMapper;

  public NotificationEventHandler(
      final ProcessNotificationEventUseCase processNotificationEventUseCase,
      final DomainEventRequestMapper requestMapper) {
    this.processNotificationEventUseCase =
        Objects.requireNonNull(processNotificationEventUseCase);
    this.requestMapper = Objects.requireNonNull(requestMapper);
  }

  @RabbitListener(
      queues = "${rabbitmq.notification.queue}",
      errorHandler = "notificationEventErrorHandler")
  public void handle(final DomainEventRequest request) {
    processNotificationEventUseCase.process(requestMapper.toCommand(Objects.requireNonNull(request)));
  }
}
