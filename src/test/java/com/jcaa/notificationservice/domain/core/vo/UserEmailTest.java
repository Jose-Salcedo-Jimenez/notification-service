package com.jcaa.notificationservice.domain.core.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.notificationservice.domain.core.exception.InvalidUserEmailException;
import org.junit.jupiter.api.Test;

class UserEmailTest {

  @Test
  void shouldNormalizeValidEmail() {
    // Arrange
    final String rawEmail = "  USER.Name+tag@Example.COM  ";

    // Act
    final UserEmail email = new UserEmail(rawEmail);

    // Assert
    assertAll(
        () -> assertThat(email.value()).isEqualTo("user.name+tag@example.com"),
        () -> assertThat(email).isEqualTo(new UserEmail("user.name+tag@example.com")),
        () -> assertThat(email.hashCode()).isEqualTo(new UserEmail(email.value()).hashCode()));
  }

  @Test
  void shouldRejectNullEmail() {
    // Arrange

    // Act
    final InvalidUserEmailException exception =
        assertThrows(InvalidUserEmailException.class, () -> new UserEmail(null));

    // Assert
    assertThat(exception.getMessage()).contains("must not be null");
  }

  @Test
  void shouldRejectBlankEmail() {
    // Arrange
    final String blankEmail = "   ";

    // Act
    final InvalidUserEmailException exception =
        assertThrows(InvalidUserEmailException.class, () -> new UserEmail(blankEmail));

    // Assert
    assertThat(exception.getMessage()).contains("must not be empty");
  }

  @Test
  void shouldRejectInvalidEmailFormat() {
    // Arrange
    final String invalidEmail = "invalid-email";

    // Act
    final InvalidUserEmailException exception =
        assertThrows(InvalidUserEmailException.class, () -> new UserEmail(invalidEmail));

    // Assert
    assertThat(exception.getMessage()).contains(invalidEmail);
  }
}
