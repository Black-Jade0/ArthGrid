package com.Prakhar.Payment_Service.Config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.Prakhar.Payment_Service.Event.PaymentEvent;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, PaymentEvent> producerFactory() {

        //  ENV PEM load
        String ca = System.getenv("KAFKA_CA_CERT").replace("\\n", "\n");
        String cert = System.getenv("KAFKA_ACCESS_CERT").replace("\\n", "\n");
        String key = System.getenv("KAFKA_ACCESS_KEY").replace("\\n", "\n");


        System.out.println("KAFKA_URL: " + System.getenv("KAFKA_URL"));
        System.out.println("CA length: " + ca.length());
        System.out.println("CERT length: " + cert.length());
        System.out.println("KEY length: " + key.length());

        Map<String, Object> config = new HashMap<>();

        //  Kafka Server
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv("KAFKA_URL"));

        // SSL (Aiven PEM)
        config.put("security.protocol", "SSL");

        config.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
        config.put(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, ca);

        config.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM");
        config.put(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, cert);
        config.put(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, key);

        config.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");

        // Serializers
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Optional
        config.put("client.id", "payment-service");

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, PaymentEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}