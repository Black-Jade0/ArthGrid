package com.Prakhar.Notification_service.Config;

import java.util.HashMap;
import java.util.Map;

import com.Prakhar.Notification_service.DTO.PaymentEvent;
import com.Prakhar.Notification_service.DTO.UserEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    /**
     * Two modes, picked automatically from environment variables:
     *  - Cloud (Aiven): KAFKA_CA_CERT, KAFKA_ACCESS_CERT and KAFKA_ACCESS_KEY are set -> SSL.
     *  - Local: those are not set -> plain connection to KAFKA_URL (default localhost:9092).
     */
    private Map<String, Object> getCommonConfig() {

        String kafkaUrl = envOrDefault("KAFKA_URL", "localhost:9092");
        String ca = System.getenv("KAFKA_CA_CERT");
        String cert = System.getenv("KAFKA_ACCESS_CERT");
        String key = System.getenv("KAFKA_ACCESS_KEY");

        Map<String, Object> config = new HashMap<>();

        // Kafka Server
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);

        // Group Id
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group-v7");

        // A brand-new consumer group reads from the beginning, so no event is missed
        // if this service starts after someone already registered / paid.
        // (Existing groups keep their saved position, so this is safe in the cloud too.)
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

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

        System.out.println("Kafka consumer -> " + kafkaUrl + (useSsl ? " (SSL)" : " (plaintext, local mode)"));
        return config;
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

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEvent>
    userKafkaListenerContainerFactory() {

        JsonDeserializer<UserEvent> deserializer =
                new JsonDeserializer<>(UserEvent.class,false);

        deserializer.addTrustedPackages("*");

        DefaultKafkaConsumerFactory<String, UserEvent> factory =
                new DefaultKafkaConsumerFactory<>(
                        getCommonConfig(),
                        new StringDeserializer(),
                        deserializer);

        ConcurrentKafkaListenerContainerFactory<String, UserEvent> listenerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();

        listenerFactory.setConsumerFactory(factory);

        return listenerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentEvent>
    paymentKafkaListenerContainerFactory() {

        JsonDeserializer<PaymentEvent> deserializer =
                new JsonDeserializer<>(PaymentEvent.class,false);

        deserializer.addTrustedPackages("*");

        DefaultKafkaConsumerFactory<String, PaymentEvent> factory =
                new DefaultKafkaConsumerFactory<>(
                        getCommonConfig(),
                        new StringDeserializer(),
                        deserializer);

        ConcurrentKafkaListenerContainerFactory<String, PaymentEvent> listenerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();

        listenerFactory.setConsumerFactory(factory);

        return listenerFactory;
    }
}
