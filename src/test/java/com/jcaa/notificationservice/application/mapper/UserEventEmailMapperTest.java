package com.jcaa.notificationservice.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.notificationservice.application.dto.command.ProcessDomainEventCommand;
import com.jcaa.notificationservice.application.exception.InvalidNotificationEventException;
import com.jcaa.notificationservice.application.model.UserEventType;
import com.jcaa.notificationservice.domain.core.model.EmailDestination;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class UserEventEmailMapperTest {

  private static final LocalDateTime OCCURRED_ON = LocalDateTime.of(2026, 10, 12, 14, 30);
  private final UserEventEmailMapper mapper = new UserEventEmailMapper();

  @Test
  void shouldMapCreatedUserEvent() {
    // Arrange
    final ProcessDomainEventCommand command = validCommand("user.created");

    // Act
    final EmailDestination destination = mapper.toEmailDestination(command, UserEventType.CREATED);

    // Assert
    assertAll(
        () -> assertThat(destination.destinationEmail().value()).isEqualTo("ana.perez@example.com"),
        () -> assertThat(destination.destinationName()).isEqualTo("Ana Perez"),
        () -> assertThat(destination.subject()).isEqualTo("Welcome to our platform"),
        () -> assertThat(destination.body()).contains("account was created successfully"));
  }

  @Test
  void shouldMapUpdatedUserEvent() {
    // Arrange
    final ProcessDomainEventCommand command = validCommand("user.updated");

    // Act
    final EmailDestination destination = mapper.toEmailDestination(command, UserEventType.UPDATED);

    // Assert
    assertAll(
        () -> assertThat(destination.subject()).isEqualTo("Your account was updated"),
        () -> assertThat(destination.body()).contains("information was updated successfully"));
  }

  @Test
  void shouldRejectNullPayload() {
    // Arrange
    final ProcessDomainEventCommand command =
        new ProcessDomainEventCommand("user.created", OCCURRED_ON, null);

    // Act
    final InvalidNotificationEventException exception =
        assertThrows(
            InvalidNotificationEventException.class,
            () -> mapper.toEmailDestination(command, UserEventType.CREATED));

    // Assert
    assertThat(exception.getMessage()).isEqualTo("The notification payload is required.");
  }

  @ParameterizedTest
  @EnumSource(value = MissingField.class)
  void shouldRejectMissingRequiredField(final MissingField missingField) {
    // Arrange
    final Map<String, String> payload =
        switch (missingField) {
          case NAME -> Map.of("email", "ana.perez@example.com");
          case EMAIL -> Map.of("name", "Ana Perez");
          case BLANK_NAME -> Map.of("name", " ", "email", "ana.perez@example.com");
        };
    final ProcessDomainEventCommand command =
        new ProcessDomainEventCommand("user.created", OCCURRED_ON, payload);

    // Act
    final InvalidNotificationEventException exception =
        assertThrows(
            InvalidNotificationEventException.class,
            () -> mapper.toEmailDestination(command, UserEventType.CREATED));

    // Assert
    assertThat(exception.getMessage())
        .isEqualTo("The notification payload is missing a required field.");
  }

  @Test
  void shouldRejectDeletedEventMapping() {
    // Arrange
    final ProcessDomainEventCommand command = validCommand("user.deleted");

    // Act
    final InvalidNotificationEventException exception =
        assertThrows(
            InvalidNotificationEventException.class,
            () -> mapper.toEmailDestination(command, UserEventType.DELETED));

    // Assert
    assertThat(exception.getMessage())
        .isEqualTo("The notification payload is missing a required field.");
  }

  private static ProcessDomainEventCommand validCommand(final String eventName) {
    return new ProcessDomainEventCommand(
        eventName,
        OCCURRED_ON,
        Map.of("name", "  Ana Perez  ", "email", "ANA.PEREZ@EXAMPLE.COM"));
  }

  private enum MissingField {
    NAME,
    EMAIL,
    BLANK_NAME
  }
}
