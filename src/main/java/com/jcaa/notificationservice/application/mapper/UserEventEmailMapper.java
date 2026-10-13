package com.jcaa.notificationservice.application.mapper;

import com.jcaa.notificationservice.application.dto.command.ProcessDomainEventCommand;
import com.jcaa.notificationservice.application.exception.InvalidNotificationEventException;
import com.jcaa.notificationservice.application.model.UserEventType;
import com.jcaa.notificationservice.domain.core.model.EmailDestination;
import com.jcaa.notificationservice.domain.core.vo.UserEmail;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public final class UserEventEmailMapper {

  private static final String EMAIL_FIELD = "email";
  private static final String NAME_FIELD = "name";
  private static final String MISSING_PAYLOAD_MESSAGE = "The notification payload is required.";
  private static final String MISSING_FIELD_MESSAGE =
      "The notification payload is missing a required field.";
  private static final String CREATED_SUBJECT = "Welcome to our platform";
  private static final String UPDATED_SUBJECT = "Your account was updated";
  private static final String CREATED_BODY_TEMPLATE =
      "<p>Hello %s, your account was created successfully.</p>";
  private static final String UPDATED_BODY_TEMPLATE =
      "<p>Hello %s, your account information was updated successfully.</p>";

  public EmailDestination toEmailDestination(
      final ProcessDomainEventCommand command, final UserEventType eventType) {
    final Map<String, String> payload = requiredPayload(command.payload());
    final String name = requiredField(payload, NAME_FIELD);
    final UserEmail email = new UserEmail(requiredField(payload, EMAIL_FIELD));
    final EmailContent content = contentFor(eventType);
    return new EmailDestination(
        email, name, content.subject(), content.bodyTemplate().formatted(name));
  }

  private static Map<String, String> requiredPayload(final Map<String, String> payload) {
    if (Objects.isNull(payload)) {
      throw new InvalidNotificationEventException(MISSING_PAYLOAD_MESSAGE);
    }
    return payload;
  }

  private static String requiredField(final Map<String, String> payload, final String fieldName) {
    final String value = payload.get(fieldName);
    if (Objects.isNull(value) || value.isBlank()) {
      throw new InvalidNotificationEventException(MISSING_FIELD_MESSAGE);
    }
    return value.strip();
  }

  private static EmailContent contentFor(final UserEventType eventType) {
    return switch (eventType) {
      case CREATED -> new EmailContent(CREATED_SUBJECT, CREATED_BODY_TEMPLATE);
      case UPDATED -> new EmailContent(UPDATED_SUBJECT, UPDATED_BODY_TEMPLATE);
      case DELETED -> throw new InvalidNotificationEventException(MISSING_FIELD_MESSAGE);
    };
  }

  private record EmailContent(String subject, String bodyTemplate) {}
}
