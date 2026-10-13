package com.jcaa.notificationservice.domain.core.vo;

import com.jcaa.notificationservice.domain.core.exception.InvalidUserEmailException;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record UserEmail(String value) {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

  public UserEmail {
    if (Objects.isNull(value)) {
      throw InvalidUserEmailException.becauseValueIsNull();
    }
    final String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
    validateNotEmpty(normalizedValue);
    validateFormat(normalizedValue);
    value = normalizedValue;
  }

  private static void validateNotEmpty(final String normalizedValue) {
    if (normalizedValue.isEmpty()) {
      throw InvalidUserEmailException.becauseValueIsEmpty();
    }
  }

  private static void validateFormat(final String normalizedValue) {
    if (!EMAIL_PATTERN.matcher(normalizedValue).matches()) {
      throw InvalidUserEmailException.becauseFormatIsInvalid(normalizedValue);
    }
  }
}
