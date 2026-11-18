package com.jcaa.notificationservice.entrypoint.event.error;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

class NotificationEventRecovererTest {

  @Test
  @SuppressWarnings("unchecked")
  void shouldLogAndNotThrowOnFailedRecord() {
    // Arrange
    final NotificationEventRecoverer recoverer = new NotificationEventRecoverer();
    final ConsumerRecord<?, ?> record = mock(ConsumerRecord.class);
    when(record.topic()).thenReturn("user-events");
    when(record.partition()).thenReturn(0);
    when(record.offset()).thenReturn(42L);
    final Exception exception = new RuntimeException("parse error");

    // Act / Assert
    assertThatCode(() -> recoverer.accept(record, exception)).doesNotThrowAnyException();
  }
}
