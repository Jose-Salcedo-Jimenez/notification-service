package com.jcaa.notificationservice.domain.core.exception;

import java.io.Serial;

public final class EmailSenderException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;
  private static final String DELIVERY_FAILURE_MESSAGE =
      "The email notification could not be delivered.";

  private EmailSenderException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public static EmailSenderException becauseDeliveryFailed(final Throwable cause) {
    return new EmailSenderException(DELIVERY_FAILURE_MESSAGE, cause);
  }
}
