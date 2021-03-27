package com.example.kafkahealthpoc.selfheal;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.example.kafkahealthpoc.config.KafkaHealthPocConfig.KAFKA_BOOTSTRAP_SERVERS;
import static com.example.kafkahealthpoc.config.KafkaHealthPocConfig.TOPIC_NAME;

/*
 * Created by jhcue on 23/03/2021
 */
@Component
@Slf4j
@Lazy
public class KafkaLivenessProbe {

    Supplier<Admin> adminSupplier = () -> Admin.create(adminProperties());

    List<String> topics = List.of(TOPIC_NAME);

    public boolean kafkaIsAvailable() {
        /*
         * Liveness means that downstream services and infrastructure are available to the app,
         * for this application it means that Kafka is available for receiving and producing events (messages)
         */
        try(Admin admin = adminSupplier.get()) {
            var result = admin.describeTopics(topics).all();
            var descriptions = result.get();
            descriptions.forEach((topic, description) ->
                log.info("Description found for topic {}: {}", topic, description)
            );
            return true;
        } catch (Exception x) {
            log.error("Error getting description of topic(s): {}; Is Kafka available?",
                    topics.stream().collect(Collectors.joining(", ")), x);
        }
        return false;
    }

    public void setAdminSupplier(Supplier<Admin> adminSupplier) {
        this.adminSupplier = adminSupplier;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    private Map<String, Object> adminProperties() {
        var props = new HashMap<String, Object>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 2000);
        props.put(AdminClientConfig.CLIENT_ID_CONFIG, "kafka-health-poc-admin");
        return props;
    }
}
