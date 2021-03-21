package com.example.kafkahealthpoc.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Created by jhcue on 21/03/2021
 */
@Configuration
public class KafkaHealthPocConfig {

    @Bean
    public NewTopic topic1() {
        return new NewTopic("topic-1", 1, (short)1);
    }
}
