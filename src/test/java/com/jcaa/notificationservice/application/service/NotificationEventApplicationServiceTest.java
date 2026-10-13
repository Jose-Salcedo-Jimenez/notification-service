package com.jcaa.notificationservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.notificationservice.application.dto.command.ProcessDomainEventCommand;
import com.jcaa.notificationservice.application.mapper.UserEventEmailMapper;
import com.jcaa.notificationservice.domain.core.model.EmailDestination;
import com.jcaa.notificationservice.domain.port.out.EmailSenderPort;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationEventApplicationServiceTest {

  @Test
  void shouldSendEmailForCreatedEvent() {
    // Arrange
    final RecordingEmailSender emailSenderPort = new RecordingEmailSender();
    final NotificationEventApplicationService service =
        new NotificationEventApplicationService(emailSenderPort, new UserEventEmailMapper());
    final ProcessDomainEventCommand command = validCommand("user.created");
    // Act
    service.process(command);

    // Assert
    final EmailDestination destination = emailSenderPort.sentEmails().getFirst();
    assertThat(destination.destinationEmail().value()).isEqualTo("ana.perez@example.com");
    assertThat(destination.subject()).isEqualTo("Welcome to our platform");
  }

  @Test
  void shouldAcknowledgeDeletedEventWithoutSendingEmail() {
    // Arrange
    final RecordingEmailSender emailSenderPort = new RecordingEmailSender();
    final NotificationEventApplicationService service =
        new NotificationEventApplicationService(emailSenderPort, new UserEventEmailMapper());
    final ProcessDomainEventCommand command =
        new ProcessDomainEventCommand(
            "user.deleted", LocalDateTime.of(2026, 10, 12, 15, 30), Map.of("id", "user-id"));

    // Act
    assertDoesNotThrow(() -> service.process(command));

    // Assert
    assertThat(emailSenderPort.sentEmails()).isEmpty();
  }

  @Test
  void shouldRejectNullCommand() {
    // Arrange
    final NotificationEventApplicationService service =
        new NotificationEventApplicationService(
            new RecordingEmailSender(), new UserEventEmailMapper());

    // Act
    final NullPointerException exception =
        assertThrows(NullPointerException.class, () -> service.process(null));

    // Assert
    assertThat(exception).isNotNull();
  }

  @Test
  void shouldRejectNullCollaborators() {
    // Arrange
    final EmailSenderPort emailSenderPort = new RecordingEmailSender();
    final UserEventEmailMapper mapper = new UserEventEmailMapper();

    // Act
    final NullPointerException missingPort =
        assertThrows(
            NullPointerException.class,
            () -> new NotificationEventApplicationService(null, mapper));
    final NullPointerException missingMapper =
        assertThrows(
            NullPointerException.class,
            () -> new NotificationEventApplicationService(emailSenderPort, null));

    // Assert
    assertThat(missingPort).isNotNull();
    assertThat(missingMapper).isNotNull();
  }

  private static ProcessDomainEventCommand validCommand(final String eventName) {
    return new ProcessDomainEventCommand(
        eventName,
        LocalDateTime.of(2026, 10, 12, 14, 30),
        Map.of("name", "Ana Perez", "email", "ana.perez@example.com"));
  }

  private static final class RecordingEmailSender implements EmailSenderPort {

    private final List<EmailDestination> sentEmails = new ArrayList<>();

    @Override
    public void send(final EmailDestination destination) {
      sentEmails.add(destination);
    }

    private List<EmailDestination> sentEmails() {
      return List.copyOf(sentEmails);
    }
  }
}
