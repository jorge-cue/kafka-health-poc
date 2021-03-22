package com.example.kafkahealthpoc.config;

import com.example.kafkahealthpoc.inbound.kafka.KafkaHealthPocConsumer;
import com.example.kafkahealthpoc.selfheal.HealthControlPanel;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.listener.LoggingErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

/*
 * Created by jhcue on 21/03/2021
 */
@Configuration
@Slf4j
public class KafkaHealthPocConfig {

    // Topic configuration

    @Bean
    public NewTopic topic1() {
        return TopicBuilder.name("topic1").partitions(1).replicas(0).build();
    }

    // Listener configuration

    @Bean
    public KafkaListenerContainerFactory<? extends MessageListenerContainer> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            ErrorHandler errorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);
        factory.setErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public ErrorHandler errorHandler() {
        return new LoggingErrorHandler();
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory(HealthControlPanel healthControlPanel) {
        return new DefaultKafkaConsumerFactory<>(consumerProps()) {
            @Override
            protected Consumer<String, String> createRawConsumer(Map<String, Object> configProps) {
                log.info("Creating KafkaHealthPocConsumer");
                return new KafkaHealthPocConsumer<>(configProps, healthControlPanel);
            }
        };
    }

    private Map<String, Object> consumerProps() {
        var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group-poc");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    // Producer configuration

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(senderProps());
    }

    private Map<String, Object> senderProps() {
        var props = new HashMap<String, Object>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> factory) {
        return new KafkaTemplate<>(factory);
    }
}
