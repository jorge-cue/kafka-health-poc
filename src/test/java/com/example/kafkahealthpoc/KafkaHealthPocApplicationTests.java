package com.example.kafkahealthpoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class KafkaHealthPocApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void smokeTest() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("status").value("UP"));
    }
}
