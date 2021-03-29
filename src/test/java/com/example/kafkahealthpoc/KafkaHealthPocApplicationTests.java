package com.example.kafkahealthpoc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KafkaHealthPocApplicationTests {

    @Autowired
    TestRestTemplate restTemplate;

    @LocalServerPort
    int localServerPort;


    @Test
    void smokeTest() throws Exception {
        final var uri = String.format("http://localhost:%d/actuator/health", localServerPort);

        final var response = restTemplate.getForEntity(uri, String.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType()),
                () -> {
                    final var objectMapper = new ObjectMapper();
                    final var jsonBody = objectMapper.readValue(response.getBody(), JsonNode.class);
                    assertNotNull(jsonBody.get("status"));
                    assertEquals("UP", jsonBody.get("status").asText());
                }
        );
    }
}
