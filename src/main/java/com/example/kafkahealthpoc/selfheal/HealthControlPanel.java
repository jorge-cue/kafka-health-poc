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

    private AtomicBoolean listenerLiveness = new AtomicBoolean(true);
    private AtomicBoolean producerLiveness = new AtomicBoolean(true);

    private final Counter listenerDeadCount;
    private final Counter producerDeadCount;

    public HealthControlPanel(MeterRegistry registry) {
        listenerDeadCount = Counter.builder(LISTENER_DEAD_COUNT).register(registry);
        producerDeadCount = Counter.builder(PRODUCER_DEAD_COUNT).register(registry);
    }

    public boolean checkLiveness() {
        return listenerLiveness.get() && producerLiveness.get();
    }

    public void setListenerNotAlive() {
        if (listenerLiveness.compareAndSet(true, false)) {
            listenerDeadCount.increment();
        }
    }

    public void setProducerNotAlive() {
        if (producerLiveness.compareAndSet(true, false)) {
            producerDeadCount.increment();
        }
    }

    public void setListenerAlive() {
        listenerLiveness.set(true);
    }

    public void setProducerAlive() {
        producerLiveness.set(true);
    }
}
