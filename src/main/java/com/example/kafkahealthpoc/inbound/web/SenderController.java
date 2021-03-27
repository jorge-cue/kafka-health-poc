package com.example.kafkahealthpoc.inbound.web;

import com.example.kafkahealthpoc.outbound.kafka.KafkaHealthPocProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Created by jhcue on 21/03/2021
 */
@RestController
@RequestMapping("/send")
@Slf4j
public class SenderController {

    private final KafkaHealthPocProducer kafkaHealthPocProducer;

    public SenderController(KafkaHealthPocProducer kafkaHealthPocProducer) {
        this.kafkaHealthPocProducer = kafkaHealthPocProducer;
    }

    @PutMapping(value = "/{message}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> putMessage(@PathVariable String message) {
        kafkaHealthPocProducer.sendMessage(message);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postMessage(@RequestBody String message) {
        var result = kafkaHealthPocProducer.sendMessage(message).completable().join();

        var topic = result.getRecordMetadata().topic();
        var partition = result.getRecordMetadata().partition();
        var offset = result.getRecordMetadata().offset();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"topic\":\"" + topic + "\"," +
                        "\"partition\":" + partition + "," +
                        "\"offset\":" + offset +
                        "}");
    }
}
