package com.example.kafkahealthpoc.inbound.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/*
 * Created by jhcue on 21/03/2021
 */
@Component
@Slf4j
public class KafkaHealthPocListener {

    @KafkaListener(
            id = "khp-id",
            groupId = "khp-group",
            topics = "topic-1"
    )
    public void handleRecord(ConsumerRecord<String, String> record) {
        log.info("processing record: {}\ttopic {} partition {} offset {} key {} value {}",
                record.value(),
                record.topic(), record.partition(), record.offset(), record.key(), record.value());
    }
}
