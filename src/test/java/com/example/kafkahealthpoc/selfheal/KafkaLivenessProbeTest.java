package com.example.kafkahealthpoc.selfheal;

import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.MockAdminClient;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.TopicBuilder;

import java.util.List;
import java.util.Map;

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

        List<Node> brokers = List.of(new Node(0, "localhost", 9092));
        mockAdminClient = new MockAdminClient(brokers, brokers.get(0));

        when(kafkaAdminClient.describeTopics(anyList())).thenAnswer(invocation ->
                mockAdminClient.describeTopics(invocation.getArgument(0)));
    }

    @Test
    void kafkaIsAvailable() {
        // Create the topic so describeTopic bellow has the topic to describe.
        mockAdminClient.createTopics(List.of(TopicBuilder.name(TOPIC_NAME).partitions(1).replicas(1).build()));

        boolean isAvailable = kafkaLivenessProbe.kafkaIsAvailable();

        assertTrue(isAvailable);
        verify(kafkaAdminClient).describeTopics(anyList());
    }

    @Test
    void kafkaIsNotAvailable() {
        boolean isAvailable = kafkaLivenessProbe.kafkaIsAvailable();

        assertFalse(isAvailable);
        verify(kafkaAdminClient).describeTopics(anyList());
    }
}