package com.jcaa.notificationservice.domain.core.exception;

import java.io.Serial;

public final class InvalidEmailDestinationException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;
  private static final String BLANK_FIELD_MESSAGE =
      "The email destination field '%s' must not be blank.";

  private InvalidEmailDestinationException(final String message) {
    super(message);
  }

  public static InvalidEmailDestinationException becauseFieldIsBlank(final String fieldName) {
    return new InvalidEmailDestinationException(String.format(BLANK_FIELD_MESSAGE, fieldName));
  }
}
