package com.example.kafkahealthpoc.selfheal;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.MockAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.kafka.config.TopicBuilder;

import java.util.List;

import static com.example.kafkahealthpoc.config.KafkaHealthPocConfig.TOPIC_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Created by jhcue on 23/03/2021
 */
@Execution(ExecutionMode.SAME_THREAD)
class KafkaLivenessProbeTest {
    // Subject Under Test
    private KafkaLivenessProbe kafkaLivenessProbe;

    Admin admin;

    NewTopic topic = TopicBuilder.name(TOPIC_NAME).partitions(1).replicas(1).build();

    @BeforeEach
    void setUp() {
        // Create Kafka environment to have one Node in the list of brokers and
        // the same Node as the controller (must be in the list of brokers)
        var node0 = new Node(0, "kafka0.example.com", 9092);
        var brokers = List.of(node0);
        admin = new MockAdminClient(brokers, node0);

        kafkaLivenessProbe = new KafkaLivenessProbe();
        kafkaLivenessProbe.setAdminSupplier(() -> admin);
    }

    @Test
    void kafkaIsAvailable_TopicExistsAndKafkaIsResponsive() {
        admin.createTopics(List.of(topic)).all()
                .whenComplete((result, throwable) -> {
                    boolean isAvailable = kafkaLivenessProbe.kafkaIsAvailable();
                    assertTrue(isAvailable);
                });
    }

    @Test
    void kafkaIsAvailable_TopicDoesNotExist() {
        // Topic is not created so mock admin client do not have any topic to describe.
        boolean isAvailable = kafkaLivenessProbe.kafkaIsAvailable();
        assertFalse(isAvailable);
    }

    @Test
    void kafkaIsAvailable_KafkaIsNotResponsive() {
        // Create the topic so describeTopic bellow has the topic to describe.
        admin.createTopics(List.of(topic)).all()
                .whenComplete((result, throwable) -> {
                    // Next request will timeout, to simulate that kafka is not available
                    ((MockAdminClient) admin).timeoutNextRequest(1);

                    boolean isAvailable = kafkaLivenessProbe.kafkaIsAvailable();
                    assertFalse(isAvailable);
                });
    }
}