package com.jcaa.notificationservice.domain.core.exception;

import java.io.Serial;

public final class InvalidUserEmailException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;
  private static final String MESSAGE_NULL = "The user email must not be null.";
  private static final String MESSAGE_EMPTY = "The user email must not be empty.";
  private static final String MESSAGE_INVALID_FORMAT = "The user email format is invalid: '%s'.";

  private InvalidUserEmailException(final String message) {
    super(message);
  }

  public static InvalidUserEmailException becauseValueIsNull() {
    return new InvalidUserEmailException(MESSAGE_NULL);
  }

  public static InvalidUserEmailException becauseValueIsEmpty() {
    return new InvalidUserEmailException(MESSAGE_EMPTY);
  }

  public static InvalidUserEmailException becauseFormatIsInvalid(final String email) {
    return new InvalidUserEmailException(String.format(MESSAGE_INVALID_FORMAT, email));
  }
}
