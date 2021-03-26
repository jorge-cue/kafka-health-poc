package com.example.kafkahealthpoc.selfheal;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/*
 * Created by jhcue on 21/03/2021
 */
@Component
@Slf4j
public class HealthControlPanel {

    private final KafkaLivenessProbe kafkaLivenessProbe;

    public static final String LISTENER_DEAD_COUNT = "listener-dead-count";
    public static final String PRODUCER_DEAD_COUNT = "producer-dead-count";

    private final AtomicBoolean listenerAvailable = new AtomicBoolean(true);
    private final AtomicBoolean producerAvailable = new AtomicBoolean(true);

    private final Counter listenerDeadCount; // Number of times listener has become Not Alive so far.
    private final Counter producerDeadCount; // Number of times producer has become Not Alive so far.

    public HealthControlPanel(MeterRegistry registry, KafkaLivenessProbe kafkaLivenessProbe) {
        listenerDeadCount = Counter.builder(LISTENER_DEAD_COUNT).register(registry);
        producerDeadCount = Counter.builder(PRODUCER_DEAD_COUNT).register(registry);
        this.kafkaLivenessProbe = kafkaLivenessProbe;
    }

    public boolean checkLiveness() {
        return listenerAvailable.get() && producerAvailable.get() && kafkaLivenessProbe.kafkaIsAvailable();
    }

    public boolean checkReadiness() {
        /*
         * Readiness means that this service is able to process incoming network traffic, for this application it means
         * that our producer is ok and Kafka is available for sending events (messages)
         */
        return producerAvailable.get() && kafkaLivenessProbe.kafkaIsAvailable();
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
}
