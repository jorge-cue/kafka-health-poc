package com.example.kafkahealthpoc.selfheal;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Created by jhcue on 21/03/2021
 */
@ExtendWith(MockitoExtension.class)
class HealthControlPanelTest {

    MeterRegistry meterRegistry;

    HealthControlPanel healthControlPanel;

    @Mock
    KafkaLivenessProbe kafkaLivenessProbe;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        healthControlPanel = new HealthControlPanel(meterRegistry, kafkaLivenessProbe);
    }

    @Test
    void defaultLivenessProveIsTrue() {
        when(kafkaLivenessProbe.kafkaIsAvailable()).thenReturn(true);
        boolean livenessProbe = healthControlPanel.checkLiveness();
        assertTrue(livenessProbe);
        verify(kafkaLivenessProbe).kafkaIsAvailable();
    }

    @Test
    void listenerSetNotAliveTwice() {
        healthControlPanel.setListenerNotAlive();
        healthControlPanel.setListenerNotAlive();

        boolean livenessProbe = healthControlPanel.checkLiveness();

        assertFalse(livenessProbe);
        Counter listenerDeadCount = meterRegistry.counter(HealthControlPanel.LISTENER_DEAD_COUNT);
        assertEquals(1.0, listenerDeadCount.count(), 1E-3);
        verify(kafkaLivenessProbe, never()).kafkaIsAvailable();
    }

    @Test
    void multiThreadListenerSetNotAlive() {
        // Setup 10 threads to set listener not alive concurrently
        var futures = IntStream.range(0, 10)
                .mapToObj(n -> CompletableFuture.runAsync(() -> healthControlPanel.setListenerNotAlive()))
                .collect(Collectors.toList());
        // Run all 10 threads in parallel
        var results = futures.stream().parallel().map(CompletableFuture::join).collect(Collectors.toList());

        // All 10 threads finished
        assertEquals(10, results.size());

        boolean livenessProbe = healthControlPanel.checkLiveness();

        // livenessProve is false
        assertFalse(livenessProbe);
        // Only one transition from alive to not alive is counted
        Counter listenerDeadCount = meterRegistry.counter(HealthControlPanel.LISTENER_DEAD_COUNT);
        assertEquals(1.0, listenerDeadCount.count(), 1E-3);
        verify(kafkaLivenessProbe, never()).kafkaIsAvailable();
    }

    @Test
    void producerSetNotAliveTwice() {
        healthControlPanel.setProducerNotAlive();
        healthControlPanel.setProducerNotAlive();

        boolean livenessProbe = healthControlPanel.checkLiveness();

        assertFalse(livenessProbe);
        Counter producerDeadCount = meterRegistry.counter(HealthControlPanel.PRODUCER_DEAD_COUNT);
        assertEquals(1.0, producerDeadCount.count(), 1E-3);
        verify(kafkaLivenessProbe, never()).kafkaIsAvailable();
    }

    @Test
    void multiThreadProducerSetNotAlive() {
        // Setup 10 threads to set listener not alive concurrently
        var futures = IntStream.range(0, 10)
                .mapToObj(n -> CompletableFuture.runAsync(() -> healthControlPanel.setProducerNotAlive()))
                .collect(Collectors.toList());
        // Run all 10 threads in parallel
        var results = futures.stream().parallel().map(CompletableFuture::join).collect(Collectors.toList());

        // All 10 threads finished
        assertEquals(10, results.size());

        boolean livenessProbe = healthControlPanel.checkLiveness();

        // livenessProve is false
        assertFalse(livenessProbe);
        // Only one transition from alive to not alive is counted
        Counter producerDeadCount = meterRegistry.counter(HealthControlPanel.PRODUCER_DEAD_COUNT);
        assertEquals(1.0, producerDeadCount.count(), 1E-3);
        verify(kafkaLivenessProbe, never()).kafkaIsAvailable();
    }
}