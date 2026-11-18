package com.jcaa.notificationservice.entrypoint.event.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.jcaa.notificationservice.application.dto.command.ProcessDomainEventCommand;
import com.jcaa.notificationservice.entrypoint.event.request.DomainEventRequest;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DomainEventRequestMapperTest {

  private final DomainEventRequestMapper mapper = new DomainEventRequestMapper();

  @Test
  void shouldMapRequestAndDefensivelyCopyPayload() {
    // Arrange
    final String occurredOn = "2026-10-12T14:30:00";
    final Map<String, String> payload = new HashMap<>(Map.of("id", "user-id"));
    final DomainEventRequest request =
        new DomainEventRequest("user.created", occurredOn, payload);

    // Act
    final ProcessDomainEventCommand command = mapper.toCommand(request);
    payload.put("name", "changed");

    // Assert
    assertAll(
        () -> assertThat(command.eventName()).isEqualTo("user.created"),
        () -> assertThat(command.occurredOn()).isEqualTo(occurredOn),
        () -> assertThat(command.payload()).containsExactlyEntriesOf(Map.of("id", "user-id")));
  }

  @Test
  void shouldMapNullPayloadToEmptyMap() {
    // Arrange
    final DomainEventRequest request =
        new DomainEventRequest("user.deleted", "2026-10-12T15:30:00Z", null);

    // Act
    final ProcessDomainEventCommand command = mapper.toCommand(request);

    // Assert
    assertThat(command.payload()).isEmpty();
  }
}
