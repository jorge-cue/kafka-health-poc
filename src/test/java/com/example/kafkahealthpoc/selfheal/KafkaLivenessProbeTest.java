package com.example.kafkahealthpoc.selfheal;

import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.MockAdminClient;
import org.apache.kafka.common.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.TopicBuilder;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.example.kafkahealthpoc.config.KafkaHealthPocConfig.TOPIC_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Created by jhcue on 23/03/2021
 */
@ExtendWith(MockitoExtension.class)
class KafkaLivenessProbeTest {

    private KafkaLivenessProbe kafkaLivenessProbe;

    @Mock
    KafkaAdminClient kafkaAdminClient;

    MockAdminClient mockAdminClient;

    @BeforeEach
    void setUp() {
        kafkaLivenessProbe = new KafkaLivenessProbe(kafkaAdminClient);
        // kafkaAdminClient mock delegates to mockAdminClient the descriptions of topics.
        when(kafkaAdminClient.describeTopics(anyList())).thenAnswer(invocation -> {
            var topics = invocation.<List<String>>getArgument(0);
            return mockAdminClient.describeTopics(topics);
        });
        // Create Kafka environment to have one Node in the list of brokers and
        // the same Node as the controller (must be in the list of brokers)
        var node0 = new Node(0, "kafka0.example.com", 9092);
        var brokers = List.of(node0);
        mockAdminClient = new MockAdminClient(brokers, node0);
    }

    @Test
    void kafkaIsAvailable_TopicExistsAndKafkaIsResponsive() {
        mockAdminClient.createTopics(List.of(TopicBuilder.name(TOPIC_NAME).partitions(1).replicas(1).build())).all()
        .whenComplete((result, throwable) -> {
            boolean isAvailable = kafkaLivenessProbe.kafkaIsAvailable();

            assertTrue(isAvailable);
            verify(kafkaAdminClient).describeTopics(anyList());
        });
    }

    @Test
    void kafkaIsAvailable_TopicDoesNotExist() {
        boolean isAvailable = kafkaLivenessProbe.kafkaIsAvailable();

        assertFalse(isAvailable);
        verify(kafkaAdminClient).describeTopics(anyList());
    }

    @Test
    void kafkaIsAvailable_KafkaIsNotResponsive() {
        // Create the topic so describeTopic bellow has the topic to describe.
        mockAdminClient.createTopics(List.of(TopicBuilder.name(TOPIC_NAME).partitions(1).replicas(1).build())).all()
                .whenComplete((result, throwable) -> {
                    // Next request will timeout, to simulate kafka not available
                    mockAdminClient.timeoutNextRequest(1);

                    boolean isAvailable = kafkaLivenessProbe.kafkaIsAvailable();

                    assertFalse(isAvailable);
                    verify(kafkaAdminClient).describeTopics(anyList());
                });
    }
}