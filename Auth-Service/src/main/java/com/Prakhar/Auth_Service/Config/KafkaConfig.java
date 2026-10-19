package com.Prakhar.Auth_Service.Config;

import java.util.HashMap;
import java.util.Map;

import com.Prakhar.Auth_Service.DTO.UserEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, UserEvent> producerFactory() {

        String kafkaUrl = System.getenv("KAFKA_URL");
        String caEnv = System.getenv("KAFKA_CA_CERT");
        String certEnv = System.getenv("KAFKA_ACCESS_CERT");
        String keyEnv = System.getenv("KAFKA_ACCESS_KEY");

        // HARD VALIDATION (important)
        if (kafkaUrl == null || caEnv == null || certEnv == null || keyEnv == null) {
            throw new RuntimeException("Kafka ENV variables missing");
        }

        String ca = caEnv.replace("\\n", "\n").trim();
        String cert = certEnv.replace("\\n", "\n").trim();
        String key = keyEnv.replace("\\n", "\n").trim();

        System.out.println("KAFKA_URL: " + kafkaUrl);
        System.out.println("CA length: " + ca.length());
        System.out.println("CERT length: " + cert.length());
        System.out.println("KEY length: " + key.length());

        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
        config.put("security.protocol", "SSL");

        config.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
        config.put(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, ca);

        config.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM");
        config.put(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, cert);
        config.put(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, key);

        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        config.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");

        config.put("client.id", "auth-service");

        try {
            return new DefaultKafkaProducerFactory<>(config);
        } catch (Exception e) {
            System.out.println("Kafka ProducerFactory creation FAILED");
            e.printStackTrace();
            throw e;
        }
    }

    @Bean
    public KafkaTemplate<String, UserEvent> kafkaTemplate(ProducerFactory<String, UserEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}