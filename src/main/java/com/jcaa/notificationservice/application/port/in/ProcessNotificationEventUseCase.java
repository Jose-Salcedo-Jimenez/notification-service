package com.jcaa.notificationservice.application.port.in;

import com.jcaa.notificationservice.application.dto.command.ProcessDomainEventCommand;

public interface ProcessNotificationEventUseCase {

  void process(ProcessDomainEventCommand command);
}
