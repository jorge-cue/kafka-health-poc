package com.example.kafkahealthpoc.outbound.kafka;

import com.example.kafkahealthpoc.selfheal.HealthControlPanel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.UUID;

import static com.example.kafkahealthpoc.config.KafkaHealthPocConfig.TOPIC_NAME;

/*
 * Created by jhcue on 22/03/2021
 */
@Component
@Slf4j
public class KafkaHealthPocProducer {

    private final HealthControlPanel healthControlPanel;

    private final KafkaTemplate<String, String> kafkaTemplate;

    // Assign all messages for this run the same key, just because!
    private final String key = UUID.randomUUID().toString();

    public KafkaHealthPocProducer(HealthControlPanel healthControlPanel, KafkaTemplate<String, String> kafkaTemplate) {
        this.healthControlPanel = healthControlPanel;
        this.kafkaTemplate = kafkaTemplate;
    }

    public ListenableFuture<SendResult<String, String>> sendMessage(String message) {
        var future = kafkaTemplate.send(TOPIC_NAME, key, message);
        future.addCallback(
                // SuccessCallback
                data -> {
                    log.info("Success sending message: {}\n\ttopic {} partition {} offset {} key {} value {}",
                            message,
                            data.getRecordMetadata().topic(),
                            data.getRecordMetadata().partition(),
                            data.getRecordMetadata().offset(),
                            data.getProducerRecord().key(),
                            data.getProducerRecord().value()
                    );
                    healthControlPanel.setProducerAlive();
                },
                // FailureCallback
                error -> {
                    log.error("Error sending message: {}, {}", message, error.getMessage(), error);
                    healthControlPanel.setProducerNotAlive();
                }
        );
        kafkaTemplate.flush();
        return future;
    }
}
