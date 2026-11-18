package com.jcaa.notificationservice.entrypoint.event.error;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;

public final class NotificationEventRecoverer implements ConsumerRecordRecoverer {

  private static final Logger log = LoggerFactory.getLogger(NotificationEventRecoverer.class);
  private static final String ERROR_LOG_MESSAGE =
      "Kafka notification event skipped. topic={}, partition={}, offset={}, cause={}";

  @Override
  public void accept(final ConsumerRecord<?, ?> consumerRecord, final Exception exception) {
    log.error(
        ERROR_LOG_MESSAGE,
        consumerRecord.topic(),
        consumerRecord.partition(),
        consumerRecord.offset(),
        exception.getClass().getSimpleName());
  }
}
