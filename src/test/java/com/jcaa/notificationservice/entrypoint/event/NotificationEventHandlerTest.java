package com.jcaa.notificationservice.entrypoint.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.notificationservice.application.dto.command.ProcessDomainEventCommand;
import com.jcaa.notificationservice.application.port.in.ProcessNotificationEventUseCase;
import com.jcaa.notificationservice.entrypoint.event.mapper.DomainEventRequestMapper;
import com.jcaa.notificationservice.entrypoint.event.request.DomainEventRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationEventHandlerTest {

  @Test
  void shouldDelegateMappedEventToUseCase() {
    // Arrange
    final RecordingUseCase useCase = new RecordingUseCase();
    final NotificationEventHandler handler =
        new NotificationEventHandler(useCase, new DomainEventRequestMapper());
    final LocalDateTime occurredOn = LocalDateTime.of(2026, 10, 12, 14, 30);
    final Map<String, String> payload = Map.of("id", "user-id");
    final DomainEventRequest request =
        new DomainEventRequest("user.created", occurredOn, payload);

    // Act
    handler.handle(request);

    // Assert
    assertThat(useCase.processedCommands())
        .containsExactly(new ProcessDomainEventCommand("user.created", occurredOn, payload));
  }

  @Test
  void shouldRejectNullRequest() {
    // Arrange
    final NotificationEventHandler handler =
        new NotificationEventHandler(
            new RecordingUseCase(), new DomainEventRequestMapper());

    // Act
    final NullPointerException exception =
        assertThrows(NullPointerException.class, () -> handler.handle(null));

    // Assert
    assertThat(exception).isNotNull();
  }

  @Test
  void shouldRejectNullCollaborators() {
    // Arrange
    final ProcessNotificationEventUseCase useCase = new RecordingUseCase();
    final DomainEventRequestMapper mapper = new DomainEventRequestMapper();

    // Act
    final NullPointerException missingUseCase =
        assertThrows(
            NullPointerException.class, () -> new NotificationEventHandler(null, mapper));
    final NullPointerException missingMapper =
        assertThrows(
            NullPointerException.class, () -> new NotificationEventHandler(useCase, null));

    // Assert
    assertThat(missingUseCase).isNotNull();
    assertThat(missingMapper).isNotNull();
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
