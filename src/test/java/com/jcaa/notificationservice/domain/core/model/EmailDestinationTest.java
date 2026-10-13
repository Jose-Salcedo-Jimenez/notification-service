package com.jcaa.notificationservice.domain.core.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.notificationservice.domain.core.exception.InvalidEmailDestinationException;
import com.jcaa.notificationservice.domain.core.vo.UserEmail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class EmailDestinationTest {

  private static final UserEmail EMAIL = new UserEmail("recipient@example.com");
  private static final String NAME = "Recipient";
  private static final String SUBJECT = "Welcome";
  private static final String BODY = "<p>Hello</p>";

  @Test
  void shouldCreateDestinationAndNormalizeText() {
    // Arrange

    // Act
    final EmailDestination destination =
        new EmailDestination(EMAIL, "  Recipient  ", "  Welcome  ", "  <p>Hello</p>  ");

    // Assert
    assertAll(
        () -> assertThat(destination.destinationEmail()).isEqualTo(EMAIL),
        () -> assertThat(destination.destinationName()).isEqualTo(NAME),
        () -> assertThat(destination.subject()).isEqualTo(SUBJECT),
        () -> assertThat(destination.body()).isEqualTo(BODY));
  }

  @Test
  void shouldRejectNullDestinationEmail() {
    // Arrange

    // Act
    final InvalidEmailDestinationException exception =
        assertThrows(
            InvalidEmailDestinationException.class,
            () -> new EmailDestination(null, NAME, SUBJECT, BODY));

    // Assert
    assertThat(exception.getMessage()).contains("destinationEmail");
  }

  @ParameterizedTest
  @MethodSource("invalidTextFields")
  void shouldRejectInvalidTextFields(
      final String name, final String subject, final String body, final String expectedField) {
    // Arrange

    // Act
    final InvalidEmailDestinationException exception =
        assertThrows(
            InvalidEmailDestinationException.class,
            () -> new EmailDestination(EMAIL, name, subject, body));

    // Assert
    assertThat(exception.getMessage()).contains(expectedField);
  }

  private static Stream<Arguments> invalidTextFields() {
    return Stream.of(
        Arguments.of(null, SUBJECT, BODY, "destinationName"),
        Arguments.of("  ", SUBJECT, BODY, "destinationName"),
        Arguments.of(NAME, null, BODY, "subject"),
        Arguments.of(NAME, "", BODY, "subject"),
        Arguments.of(NAME, SUBJECT, null, "body"),
        Arguments.of(NAME, SUBJECT, "\t", "body"));
  }
}
