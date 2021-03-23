package com.example.kafkahealthpoc.selfheal;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.kafkahealthpoc.config.KafkaHealthPocConfig.KAFKA_BOOTSTRAP_SERVERS;
import static com.example.kafkahealthpoc.config.KafkaHealthPocConfig.TOPIC_NAME;

/*
 * Created by jhcue on 23/03/2021
 */
@Component
@Slf4j
public class KafkaLivenessProbe {

    public boolean kafkaIsAvailable() {
        /*
         * Liveness means that downstream services and infrastructure are available to the app,
         * for this application it means that Kafka is available for receiving and producing events (messages)
         */
        try(KafkaAdminClient adminClient = (KafkaAdminClient) AdminClient.create(adminProperties())) {
            var result = adminClient.describeTopics(List.of(TOPIC_NAME)).values();
            var topicDescription = result.get(TOPIC_NAME).get();
            log.info("Description found for topic {}", topicDescription);
            return true;
        } catch (Exception x) {
            log.error("Error getting description of our topic(s); Is Kafka available?", x);
        }
        return false;
    }

    private Map<String, Object> adminProperties() {
        var props = new HashMap<String, Object>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 2000);
        props.put(AdminClientConfig.CLIENT_ID_CONFIG, "khp-admin");
        return props;
    }
}
