package com.jcaa.notificationservice.domain.core.exception;

import java.io.Serial;

public abstract class DomainException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  protected DomainException(final String message) {
    super(message);
  }

  protected DomainException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
