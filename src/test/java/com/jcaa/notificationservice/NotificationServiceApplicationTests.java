package com.jcaa.notificationservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.jcaa.notificationservice.adapter.email.config.BrevoProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

class NotificationServiceApplicationTests {

  @Test
  void contextLoads() {
    // Arrange
    final SpringApplication application =
        new SpringApplication(NotificationServiceApplication.class);
    application.setWebApplicationType(WebApplicationType.NONE);

    // Act
    try (ConfigurableApplicationContext context =
        application.run(
            "--spring.config.import=",
            "--spring.rabbitmq.addresses=localhost:5672",
            "--brevo.local.api-key=xkeysib-test-api-key",
            "--brevo.local.from.address=no-reply@example.com",
            "--brevo.local.from.name=Test Notification Service",
            "--spring.rabbitmq.listener.simple.auto-startup=false")) {

      // Assert
      final BrevoProperties properties = context.getBean(BrevoProperties.class);
      assertThat(context.isActive()).isTrue();
      assertThat(properties.apiKey()).isEqualTo("xkeysib-test-api-key");
      assertThat(properties.fromAddress()).isEqualTo("no-reply@example.com");
      assertThat(properties.fromName()).isEqualTo("Test Notification Service");
    }
  }

}
