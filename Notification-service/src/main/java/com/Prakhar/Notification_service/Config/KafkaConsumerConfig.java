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

    private Map<String, Object> getCommonConfig() {

        String ca = System.getenv("KAFKA_CA_CERT").replace("\\n", "\n");
        String cert = System.getenv("KAFKA_ACCESS_CERT").replace("\\n", "\n");
        String key = System.getenv("KAFKA_ACCESS_KEY").replace("\\n", "\n");

        Map<String, Object> config = new HashMap<>();

        // Kafka Server
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                System.getenv("KAFKA_URL"));

        // Group Id
        config.put(ConsumerConfig.GROUP_ID_CONFIG,
                "notification-group-v7");

        // Offset
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "latest");

        // SSL
        config.put("security.protocol", "SSL");

        config.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
        config.put(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, ca);

        config.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM");
        config.put(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, cert);

        config.put(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, key);

        config.put(
                SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,
                "https"
        );



        return config;
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
