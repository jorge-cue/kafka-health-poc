package com.example.kafkahealthpoc.selfheal;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * Created by jhcue on 21/03/2021
 */
@Component
@Slf4j
public class HealthControlPanel {

    public static final String LISTENER_DEAD_COUNT = "listener-dead-count";
    public static final String PRODUCER_DEAD_COUNT = "producer-dead-count";
    public static final String MY_TOPIC = "topic-1";

    private final AtomicBoolean listenerAvailable = new AtomicBoolean(true);
    private final AtomicBoolean producerAvailable = new AtomicBoolean(true);

    private final Counter listenerDeadCount;
    private final Counter producerDeadCount;

    public HealthControlPanel(MeterRegistry registry) {
        listenerDeadCount = Counter.builder(LISTENER_DEAD_COUNT).register(registry);
        producerDeadCount = Counter.builder(PRODUCER_DEAD_COUNT).register(registry);
    }

    public boolean checkLiveness() {
        return listenerAvailable.get() && producerAvailable.get() && kafkaIsAvailable();
    }

    public boolean checkReadiness() {
        /*
         * Readiness means that this service is able to process incoming network traffic,
         * for this application it means that Kafka is available for sending events (messages)
         */
        return producerAvailable.get();
    }

    public void setListenerNotAlive() {
        if (listenerAvailable.compareAndSet(true, false)) {
            listenerDeadCount.increment();
        }
    }

    public void setProducerNotAlive() {
        if (producerAvailable.compareAndSet(true, false)) {
            producerDeadCount.increment();
        }
    }

    public void setListenerAlive() {
        listenerAvailable.set(true);
    }

    public void setProducerAlive() {
        producerAvailable.set(true);
    }

    private boolean kafkaIsAvailable() {
        /*
         * Liveness means that downstream services and infrastructure are available to the app,
         * for this application it means that Kafka is available for receiving and producing events (messages)
         */
        try(KafkaAdminClient adminClient = (KafkaAdminClient) AdminClient.create(adminProperties())) {
            var result = adminClient.describeTopics(List.of(MY_TOPIC)).all();
            var descriptionMap = result.get();
            log.info("Description found for topic {}", descriptionMap.get(MY_TOPIC));
            return descriptionMap.containsKey(MY_TOPIC);
        } catch (Exception x) {
            log.error("Error getting description of our topic(s); Is Kafka available?", x);
        }
        return false;
    }

    private Map<String, Object> adminProperties() {
        var props = new HashMap<String, Object>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        return props;
    }

}
