package com.jcaa.notificationservice.entrypoint.event.error;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.listener.ListenerExecutionFailedException;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.stereotype.Component;

@Component
public final class NotificationEventErrorHandler implements RabbitListenerErrorHandler {

  private static final Logger log = LoggerFactory.getLogger(NotificationEventErrorHandler.class);
  private static final String ERROR_LOG_MESSAGE =
      "RabbitMQ notification event processing failed. routingKey={}, cause={}";
  private static final String REJECTION_MESSAGE = "The notification event could not be processed.";

  @Override
  public Object handleError(
      final Message amqpMessage,
      final Channel channel,
      final org.springframework.messaging.Message<?> message,
      final ListenerExecutionFailedException exception) {
    log.error(
        ERROR_LOG_MESSAGE,
        amqpMessage.getMessageProperties().getReceivedRoutingKey(),
        exception.getClass().getSimpleName());
    throw new AmqpRejectAndDontRequeueException(REJECTION_MESSAGE, exception);
  }
}
