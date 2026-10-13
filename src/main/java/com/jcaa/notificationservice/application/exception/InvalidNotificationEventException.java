package com.jcaa.notificationservice.application.exception;

import java.io.Serial;

public final class InvalidNotificationEventException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public InvalidNotificationEventException(final String message) {
    super(message);
  }
}
