package com.example.kafkahealthpoc.selfheal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/*
 * Created by jhcue on 24/03/2021
 */
@Component("kafkaLivenessProbe")
@Slf4j
@Profile("test")
public class TestKafkaLivenessProbe {

    public boolean kafkaIsAvailable() {
        return true;
    }
}
