package com.jcaa.notificationservice.application.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.notificationservice.application.exception.InvalidNotificationEventException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class UserEventTypeTest {

  @ParameterizedTest
  @CsvSource({
    "user.created, CREATED",
    "user.updated, UPDATED",
    "user.deleted, DELETED"
  })
  void shouldResolveSupportedEvent(final String eventName, final UserEventType expectedType) {
    // Arrange

    // Act
    final UserEventType eventType = UserEventType.from(eventName);

    // Assert
    assertThat(eventType).isEqualTo(expectedType);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void shouldRejectMissingEventName(final String eventName) {
    // Arrange

    // Act
    final InvalidNotificationEventException exception =
        assertThrows(InvalidNotificationEventException.class, () -> UserEventType.from(eventName));

    // Assert
    assertThat(exception.getMessage()).isEqualTo("Unsupported notification event.");
  }

  @Test
  void shouldRejectUnknownEventName() {
    // Arrange

    // Act
    final InvalidNotificationEventException exception =
        assertThrows(
            InvalidNotificationEventException.class, () -> UserEventType.from("order.created"));

    // Assert
    assertThat(exception.getMessage()).isEqualTo("Unsupported notification event.");
  }
}
