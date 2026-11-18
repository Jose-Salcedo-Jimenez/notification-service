package com.jcaa.notificationservice.entrypoint.event.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "kafka")
public record KafkaConsumerProperties(
    String bootstrapServers,
    String groupId,
    String securityProtocol,
    String saslMechanism,
    String username,
    String password,
    String caCert) {

  private static final String INVALID_PROPERTIES_MESSAGE =
      "Kafka consumer properties must not be blank.";
  private static final String JAAS_TEMPLATE =
      "org.apache.kafka.common.security.scram.ScramLoginModule required "
          + "username=\"%s\" password=\"%s\";";
  private static final String PEM_TRUSTSTORE_TYPE = "PEM";
  private static final String EARLIEST_OFFSET = "earliest";

  public KafkaConsumerProperties {
    requireText(bootstrapServers);
    requireText(groupId);
    requireText(securityProtocol);
    requireText(saslMechanism);
  }

  public Map<String, Object> toConsumerConfig() {
    final Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST_OFFSET);
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
    if (StringUtils.hasText(username)) {
      config.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
      config.put(SaslConfigs.SASL_JAAS_CONFIG, JAAS_TEMPLATE.formatted(username, password));
    }
    if (StringUtils.hasText(caCert)) {
      config.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, PEM_TRUSTSTORE_TYPE);
      config.put(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, caCert);
    }
    return Map.copyOf(config);
  }

  private static void requireText(final String value) {
    if (!StringUtils.hasText(value)) {
      throw new IllegalArgumentException(INVALID_PROPERTIES_MESSAGE);
    }
  }
}
