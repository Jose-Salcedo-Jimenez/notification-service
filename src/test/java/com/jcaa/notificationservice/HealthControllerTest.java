package com.jcaa.notificationservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HealthControllerTest {

  @Test
  void shouldReturnUpStatus() {
    // Arrange
    final HealthController controller = new HealthController();

    // Act
    final String status = controller.health().get("status");

    // Assert
    assertThat(status).isEqualTo("UP");
  }
}
