package com.jcaa.notificationservice.entrypoint.event.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KafkaConsumerPropertiesTest {

  private static final String BOOTSTRAP = "localhost:9092";
  private static final String GROUP = "notification-service";
  private static final String PROTOCOL = "PLAINTEXT";
  private static final String MECHANISM = "SCRAM-SHA-256";

  @Test
  void shouldBuildBasicConsumerConfig() {
    // Arrange
    final KafkaConsumerProperties props =
        new KafkaConsumerProperties(BOOTSTRAP, GROUP, PROTOCOL, MECHANISM, "", "", "");

    // Act
    final Map<String, Object> config = props.toConsumerConfig();

    // Assert
    assertThat(config).containsEntry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP);
    assertThat(config).containsEntry(ConsumerConfig.GROUP_ID_CONFIG, GROUP);
    assertThat(config).containsEntry(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    assertThat(config).doesNotContainKey(SaslConfigs.SASL_MECHANISM);
    assertThat(config).doesNotContainKey(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG);
  }

  @Test
  void shouldIncludeSaslConfigWhenUsernamePresent() {
    // Arrange
    final KafkaConsumerProperties props =
        new KafkaConsumerProperties(
            BOOTSTRAP, GROUP, "SASL_SSL", MECHANISM, "user", "secret", "");

    // Act
    final Map<String, Object> config = props.toConsumerConfig();

    // Assert
    assertThat(config).containsKey(SaslConfigs.SASL_MECHANISM);
    assertThat(config).containsKey(SaslConfigs.SASL_JAAS_CONFIG);
    final String jaas = (String) config.get(SaslConfigs.SASL_JAAS_CONFIG);
    assertThat(jaas).contains("user").contains("secret");
  }

  @Test
  void shouldIncludeSslConfigWhenCaCertPresent() {
    // Arrange
    final KafkaConsumerProperties props =
        new KafkaConsumerProperties(
            BOOTSTRAP, GROUP, "SASL_SSL", MECHANISM, "", "", "-----BEGIN CERTIFICATE-----\nABC\n-----END CERTIFICATE-----");

    // Act
    final Map<String, Object> config = props.toConsumerConfig();

    // Assert
    assertThat(config).containsEntry(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
    assertThat(config).containsKey(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG);
  }

  @Test
  void shouldReturnImmutableConfig() {
    // Arrange
    final KafkaConsumerProperties props =
        new KafkaConsumerProperties(BOOTSTRAP, GROUP, PROTOCOL, MECHANISM, "", "", "");

    // Act
    final Map<String, Object> config = props.toConsumerConfig();

    // Assert
    assertThrows(UnsupportedOperationException.class, () -> config.put("new-key", "value"));
  }

  @ParameterizedTest
  @CsvSource({
    "'', notification-service, PLAINTEXT, SCRAM-SHA-256",
    "localhost:9092, '', PLAINTEXT, SCRAM-SHA-256",
    "localhost:9092, notification-service, '', SCRAM-SHA-256",
    "localhost:9092, notification-service, PLAINTEXT, ''"
  })
  void shouldRejectBlankRequiredFields(
      final String bootstrap,
      final String group,
      final String protocol,
      final String mechanism) {
    // Arrange / Act / Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> new KafkaConsumerProperties(bootstrap, group, protocol, mechanism, "", "", ""));
  }
}
