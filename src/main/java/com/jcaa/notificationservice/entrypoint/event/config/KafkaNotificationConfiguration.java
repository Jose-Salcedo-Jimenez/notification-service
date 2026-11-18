package com.jcaa.notificationservice.entrypoint.event.config;

import com.jcaa.notificationservice.application.exception.InvalidNotificationEventException;
import com.jcaa.notificationservice.domain.core.exception.DomainException;
import com.jcaa.notificationservice.entrypoint.event.error.NotificationEventRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaNotificationConfiguration {

  private static final long RETRY_INTERVAL_MS = 1_000L;
  private static final long RETRY_ATTEMPTS = 2L;

  @Bean
  ConsumerFactory<String, String> notificationConsumerFactory(
      final KafkaConsumerProperties properties) {
    return new DefaultKafkaConsumerFactory<>(properties.toConsumerConfig());
  }

  @Bean
  ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
      final ConsumerFactory<String, String> notificationConsumerFactory,
      final DefaultErrorHandler notificationKafkaErrorHandler) {
    final ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(notificationConsumerFactory);
    factory.setCommonErrorHandler(notificationKafkaErrorHandler);
    return factory;
  }

  @Bean
  DefaultErrorHandler notificationKafkaErrorHandler() {
    final DefaultErrorHandler errorHandler =
        new DefaultErrorHandler(
            new NotificationEventRecoverer(), new FixedBackOff(RETRY_INTERVAL_MS, RETRY_ATTEMPTS));
    errorHandler.addNotRetryableExceptions(
        InvalidNotificationEventException.class, DomainException.class);
    return errorHandler;
  }
}
