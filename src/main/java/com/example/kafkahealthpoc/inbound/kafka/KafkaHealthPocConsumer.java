package com.example.kafkahealthpoc.inbound.kafka;

import com.example.kafkahealthpoc.selfheal.HealthControlPanel;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;

import java.time.Duration;
import java.util.Map;

/*
 * Created by jhcue on 21/03/2021
 */
@Slf4j
public class KafkaHealthPocConsumer<K, V> extends KafkaConsumer<K, V> {

    private final HealthControlPanel healthControlPanel;

    public KafkaHealthPocConsumer(Map<String, Object> configs, HealthControlPanel healthControlPanel) {
        super(configs);
        this.healthControlPanel = healthControlPanel;
    }

    @Override
    public ConsumerRecords<K, V> poll(Duration timeout) {
        try {
            log.info("polling for records.");
            var records =  super.poll(timeout);
            log.info("Records polled {}", records.count());
            healthControlPanel.setListenerAlive();
            return records;
        } catch (KafkaException kafkaException) {
            log.error("KafkaException polling from Kafka topics {}: {}",
                    String.join(", ", listTopics().keySet()), kafkaException.getMessage(), kafkaException);
            healthControlPanel.setListenerNotAlive();
            throw kafkaException;
        } catch(Exception exception) {
            log.error("Exception polling from Kafka topics {}: {}",
                    String.join(", ", listTopics().keySet()), exception.getMessage(), exception);
            healthControlPanel.setListenerNotAlive();
            throw exception;
        }
    }
}
