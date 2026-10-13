package com.jcaa.notificationservice.entrypoint.event.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.listener.ListenerExecutionFailedException;

class NotificationEventErrorHandlerTest {

  @Test
  void shouldRejectFailedMessageWithoutRequeue() {
    // Arrange
    final NotificationEventErrorHandler errorHandler = new NotificationEventErrorHandler();
    final MessageProperties messageProperties = new MessageProperties();
    messageProperties.setReceivedRoutingKey("user.created");
    final Message amqpMessage = new Message(new byte[0], messageProperties);
    final ListenerExecutionFailedException failure =
        new ListenerExecutionFailedException("Listener failed", new IllegalStateException());

    // Act
    final AmqpRejectAndDontRequeueException rejection =
        assertThrows(
            AmqpRejectAndDontRequeueException.class,
            () -> errorHandler.handleError(amqpMessage, null, null, failure));

    // Assert
    assertThat(rejection.getMessage()).isEqualTo("The notification event could not be processed.");
    assertThat(rejection.getCause()).isSameAs(failure);
  }
}
