package com.Prakhar.Payment_Service.Config;

import com.Prakhar.Payment_Service.Event.PaymentEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka producer setup.
 *
 * Two modes, picked automatically from environment variables:
 *  - Cloud (Aiven): KAFKA_CA_CERT, KAFKA_ACCESS_CERT and KAFKA_ACCESS_KEY are set -> SSL.
 *  - Local: those are not set -> plain connection to KAFKA_URL (default localhost:9092).
 */
@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, PaymentEvent> producerFactory() {

        String kafkaUrl = envOrDefault("KAFKA_URL", "localhost:9092");
        String ca = System.getenv("KAFKA_CA_CERT");
        String cert = System.getenv("KAFKA_ACCESS_CERT");
        String key = System.getenv("KAFKA_ACCESS_KEY");

        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put("client.id", "payment-service");
        // Don't hang a payment request for a full minute if Kafka is down
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10000);

        boolean useSsl = isSet(ca) && isSet(cert) && isSet(key);
        if (useSsl) {
            config.put("security.protocol", "SSL");
            config.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
            config.put(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, pem(ca));
            config.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM");
            config.put(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, pem(cert));
            config.put(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, pem(key));
            config.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");
        }

        System.out.println("Kafka producer -> " + kafkaUrl + (useSsl ? " (SSL)" : " (plaintext, local mode)"));
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, PaymentEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    private static String pem(String value) {
        return value.replace("\\n", "\n").trim();
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }

    private static String envOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return isSet(value) ? value : fallback;
    }
}
