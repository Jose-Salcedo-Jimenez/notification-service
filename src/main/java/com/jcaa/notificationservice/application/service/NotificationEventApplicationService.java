package com.jcaa.notificationservice.application.service;

import com.jcaa.notificationservice.application.dto.command.ProcessDomainEventCommand;
import com.jcaa.notificationservice.application.mapper.UserEventEmailMapper;
import com.jcaa.notificationservice.application.model.UserEventType;
import com.jcaa.notificationservice.application.port.in.ProcessNotificationEventUseCase;
import com.jcaa.notificationservice.domain.port.out.EmailSenderPort;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public final class NotificationEventApplicationService
    implements ProcessNotificationEventUseCase {

  private final EmailSenderPort emailSenderPort;
  private final UserEventEmailMapper emailMapper;

  public NotificationEventApplicationService(
      final EmailSenderPort emailSenderPort, final UserEventEmailMapper emailMapper) {
    this.emailSenderPort = Objects.requireNonNull(emailSenderPort);
    this.emailMapper = Objects.requireNonNull(emailMapper);
  }

  @Override
  public void process(final ProcessDomainEventCommand command) {
    Objects.requireNonNull(command);
    final UserEventType eventType = UserEventType.from(command.eventName());
    if (eventType == UserEventType.DELETED) {
      return;
    }
    emailSenderPort.send(emailMapper.toEmailDestination(command, eventType));
  }
}
