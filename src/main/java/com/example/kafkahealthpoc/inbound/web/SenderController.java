package com.example.kafkahealthpoc.inbound.web;

import com.example.kafkahealthpoc.selfheal.HealthControlPanel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.example.kafkahealthpoc.config.KafkaHealthPocConfig.TOPIC_NAME;

/*
 * Created by jhcue on 21/03/2021
 */
@RestController
@RequestMapping("/send")
@Slf4j
public class SenderController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    // Assign all messages for this run the same key, just because!
    private final String key = UUID.randomUUID().toString();

    private final HealthControlPanel healthControlPanel;

    public SenderController(KafkaTemplate<String, String> kafkaTemplate, HealthControlPanel healthControlPanel) {
        this.kafkaTemplate = kafkaTemplate;
        this.healthControlPanel = healthControlPanel;
    }

    @PutMapping(value = "/{message}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> putMessage(@PathVariable String message) {
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
                    healthControlPanel.setListenerAlive();
                },
                // FailureCallback
                error -> {
                    log.error("Error sending message: {}, {}", message, error.getMessage(), error);
                    healthControlPanel.setListenerNotAlive();
                }
        );
        kafkaTemplate.flush();
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
