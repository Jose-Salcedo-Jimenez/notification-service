package com.jcaa.notificationservice.entrypoint.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jcaa.notificationservice.application.dto.command.ProcessDomainEventCommand;
import com.jcaa.notificationservice.application.exception.InvalidNotificationEventException;
import com.jcaa.notificationservice.application.port.in.ProcessNotificationEventUseCase;
import com.jcaa.notificationservice.entrypoint.event.mapper.DomainEventRequestMapper;
import com.jcaa.notificationservice.entrypoint.event.request.DomainEventRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

class NotificationEventHandlerTest {

  private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

  @Test
  void shouldDelegateParsedEventToUseCase() {
    // Arrange
    final RecordingUseCase useCase = new RecordingUseCase();
    final NotificationEventHandler handler =
        new NotificationEventHandler(useCase, new DomainEventRequestMapper(), JSON_MAPPER);
    final String message =
        "{\"eventName\":\"user.created\","
            + "\"occurredOn\":\"2026-10-12T14:30:00\","
            + "\"payload\":{\"id\":\"user-id\"}}";

    // Act
    handler.handle(message);

    // Assert
    assertThat(useCase.processedCommands()).hasSize(1);
    final ProcessDomainEventCommand cmd = useCase.processedCommands().getFirst();
    assertThat(cmd.eventName()).isEqualTo("user.created");
    assertThat(cmd.payload()).containsEntry("id", "user-id");
  }

  @Test
  void shouldRejectNullMessage() {
    // Arrange
    final NotificationEventHandler handler =
        new NotificationEventHandler(
            new RecordingUseCase(), new DomainEventRequestMapper(), JSON_MAPPER);

    // Act
    final InvalidNotificationEventException exception =
        assertThrows(
            InvalidNotificationEventException.class, () -> handler.handle(null));

    // Assert
    assertThat(exception.getMessage()).isEqualTo("The notification event could not be parsed.");
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldRejectMessageThatFailsJsonParsing() throws Exception {
    // Arrange — use a mock JsonMapper that throws JacksonException
    final JsonMapper failingMapper = mock(JsonMapper.class);
    when(failingMapper.readValue(any(String.class), eq(DomainEventRequest.class)))
        .thenThrow(mock(JacksonException.class));
    final NotificationEventHandler handler =
        new NotificationEventHandler(
            new RecordingUseCase(), new DomainEventRequestMapper(), failingMapper);

    // Act
    final InvalidNotificationEventException exception =
        assertThrows(
            InvalidNotificationEventException.class, () -> handler.handle("any-string"));

    // Assert
    assertThat(exception.getMessage()).isEqualTo("The notification event could not be parsed.");
  }

  @Test
  void shouldRejectNullCollaborators() {
    // Arrange
    final ProcessNotificationEventUseCase useCase = new RecordingUseCase();
    final DomainEventRequestMapper mapper = new DomainEventRequestMapper();

    // Act
    final NullPointerException missingUseCase =
        assertThrows(
            NullPointerException.class,
            () -> new NotificationEventHandler(null, mapper, JSON_MAPPER));
    final NullPointerException missingMapper =
        assertThrows(
            NullPointerException.class,
            () -> new NotificationEventHandler(useCase, null, JSON_MAPPER));
    final NullPointerException missingJsonMapper =
        assertThrows(
            NullPointerException.class,
            () -> new NotificationEventHandler(useCase, mapper, null));

    // Assert
    assertThat(missingUseCase).isNotNull();
    assertThat(missingMapper).isNotNull();
    assertThat(missingJsonMapper).isNotNull();
  }

  private static final class RecordingUseCase implements ProcessNotificationEventUseCase {

    private final List<ProcessDomainEventCommand> processedCommands = new ArrayList<>();

    @Override
    public void process(final ProcessDomainEventCommand command) {
      processedCommands.add(command);
    }

    private List<ProcessDomainEventCommand> processedCommands() {
      return List.copyOf(processedCommands);
    }
  }
}
