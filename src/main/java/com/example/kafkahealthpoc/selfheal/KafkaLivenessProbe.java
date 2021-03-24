package com.example.kafkahealthpoc.selfheal;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.kafkahealthpoc.config.KafkaHealthPocConfig.TOPIC_NAME;

/*
 * Created by jhcue on 23/03/2021
 */
@Component
@Slf4j
public class KafkaLivenessProbe {

    private final KafkaAdminClient kafkaAdminClient;

    public KafkaLivenessProbe(KafkaAdminClient kafkaAdminClient) {
        this.kafkaAdminClient = kafkaAdminClient;
    }

    public boolean kafkaIsAvailable() {
        /*
         * Liveness means that downstream services and infrastructure are available to the app,
         * for this application it means that Kafka is available for receiving and producing events (messages)
         */
        try {
            var result = kafkaAdminClient.describeTopics(List.of(TOPIC_NAME)).values();
            var topicDescription = result.get(TOPIC_NAME).get();
            log.info("Description found for topic {}", topicDescription);
            return true;
        } catch (Exception x) {
            log.error("Error getting description of our topic(s); Is Kafka available?", x);
        }
        return false;
    }
}
