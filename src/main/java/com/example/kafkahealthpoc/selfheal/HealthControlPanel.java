package com.example.kafkahealthpoc.selfheal;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Created by jhcue on 21/03/2021
 */
@Component
public class HealthControlPanel {

    public static final String LISTENER_DEAD_COUNT = "listener-dead-count";
    public static final String PRODUCER_DEAD_COUNT = "producer-dead-count";

    private final Lock lock = new ReentrantLock();
    private boolean listenerLiveness = true;
    private boolean producerLiveness = true;

    private final Counter listenerDeadCount;
    private final Counter producerDeadCount;

    public HealthControlPanel(MeterRegistry registry) {
        listenerDeadCount = Counter.builder(LISTENER_DEAD_COUNT).register(registry);
        producerDeadCount = Counter.builder(PRODUCER_DEAD_COUNT).register(registry);
    }

    public boolean checkLiveness() {
        lock.lock();
        try {
            return listenerLiveness && producerLiveness;
        } finally {
            lock.unlock();
        }
    }

    public void setListenerNotAlive() {
        lock.lock();
        try {
            if (listenerLiveness) {
                listenerDeadCount.increment();
                listenerLiveness = false;
            }
        } finally {
            lock.unlock();
        }
    }

    public void setProducerNotAlive() {
        lock.lock();
        try {
            if (producerLiveness) {
                producerDeadCount.increment();
                producerLiveness = false;
            }
        } finally {
            lock.unlock();
        }
    }

    public void setListenerAlive() {
        lock.lock();
        try {
            listenerLiveness = true;
        } finally {
            lock.unlock();
        }
    }

    public void setProducerAlive() {
        lock.lock();
        try {
            producerLiveness = true;
        } finally {
            lock.unlock();
        }
    }
}
