package com.jcaa.notificationservice.entrypoint.event.mapper;

import com.jcaa.notificationservice.application.dto.command.ProcessDomainEventCommand;
import com.jcaa.notificationservice.entrypoint.event.request.DomainEventRequest;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public final class DomainEventRequestMapper {

  public ProcessDomainEventCommand toCommand(final DomainEventRequest request) {
    return new ProcessDomainEventCommand(
        request.eventName(), request.occurredOn(), immutablePayload(request.payload()));
  }

  private static Map<String, String> immutablePayload(final Map<String, String> payload) {
    return Objects.isNull(payload) ? Map.of() : Map.copyOf(payload);
  }
}
