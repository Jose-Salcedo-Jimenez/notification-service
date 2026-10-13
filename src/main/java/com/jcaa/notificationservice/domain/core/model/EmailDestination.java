package com.jcaa.notificationservice.domain.core.model;

import com.jcaa.notificationservice.domain.core.exception.InvalidEmailDestinationException;
import com.jcaa.notificationservice.domain.core.vo.UserEmail;

import java.util.Objects;

public final class EmailDestination {

  private static final String DESTINATION_EMAIL_FIELD = "destinationEmail";
  private static final String DESTINATION_NAME_FIELD = "destinationName";
  private static final String SUBJECT_FIELD = "subject";
  private static final String BODY_FIELD = "body";

  private final UserEmail destinationEmail;
  private final String destinationName;
  private final String subject;
  private final String body;

  public EmailDestination(
      final UserEmail destinationEmail,
      final String destinationName,
      final String subject,
      final String body) {
    if (Objects.isNull(destinationEmail)) {
      throw InvalidEmailDestinationException.becauseFieldIsBlank(DESTINATION_EMAIL_FIELD);
    }
    this.destinationEmail = destinationEmail;
    this.destinationName = validateNotBlank(destinationName, DESTINATION_NAME_FIELD);
    this.subject = validateNotBlank(subject, SUBJECT_FIELD);
    this.body = validateNotBlank(body, BODY_FIELD);
  }

  public UserEmail destinationEmail() {
    return destinationEmail;
  }

  public String destinationName() {
    return destinationName;
  }

  public String subject() {
    return subject;
  }

  public String body() {
    return body;
  }

  private static String validateNotBlank(final String value, final String fieldName) {
    if (Objects.isNull(value) || value.isBlank()) {
      throw InvalidEmailDestinationException.becauseFieldIsBlank(fieldName);
    }
    return value.trim();
  }
}
