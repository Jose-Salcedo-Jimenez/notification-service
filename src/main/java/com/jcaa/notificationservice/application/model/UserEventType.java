package com.jcaa.notificationservice.application.model;

import com.jcaa.notificationservice.application.exception.InvalidNotificationEventException;
import java.util.Arrays;
import java.util.Objects;

public enum UserEventType {
  CREATED("user.created"),
  UPDATED("user.updated"),
  DELETED("user.deleted");

  private static final String UNKNOWN_EVENT_MESSAGE = "Unsupported notification event.";

  private final String eventName;

  UserEventType(final String eventName) {
    this.eventName = eventName;
  }

  public static UserEventType from(final String eventName) {
    if (Objects.isNull(eventName)) {
      throw new InvalidNotificationEventException(UNKNOWN_EVENT_MESSAGE);
    }
    return Arrays.stream(values())
        .filter(eventType -> eventType.eventName.equals(eventName))
        .findFirst()
        .orElseThrow(() -> new InvalidNotificationEventException(UNKNOWN_EVENT_MESSAGE));
  }
}
