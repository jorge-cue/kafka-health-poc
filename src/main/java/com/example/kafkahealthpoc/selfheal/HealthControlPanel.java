package com.example.kafkahealthpoc.selfheal;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/*
 * Created by jhcue on 21/03/2021
 */
@Component
public class HealthControlPanel {

    public static final String LISTENER_DEAD_COUNT = "listener-dead-count";
    public static final String PRODUCER_DEAD_COUNT = "producer-dead-count";

    private AtomicBoolean listenerAvailable = new AtomicBoolean(true);
    private AtomicBoolean producerAvailable = new AtomicBoolean(true);

    private final Counter listenerDeadCount;
    private final Counter producerDeadCount;

    public HealthControlPanel(MeterRegistry registry) {
        listenerDeadCount = Counter.builder(LISTENER_DEAD_COUNT).register(registry);
        producerDeadCount = Counter.builder(PRODUCER_DEAD_COUNT).register(registry);
    }

    public boolean checkLiveness() {
        /*
         * Liveness means that downstream services and infrastructure are available to the app,
         * for this application it means that Kafka is available for receiving and producing events (messages)
         */
        return listenerAvailable.get() && producerAvailable.get();
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
}
