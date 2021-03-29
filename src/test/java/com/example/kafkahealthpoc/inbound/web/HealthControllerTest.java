package com.example.kafkahealthpoc.inbound.web;

import com.example.kafkahealthpoc.selfheal.HealthControlPanel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Created by jhcue on 21/03/2021
 */
@WebMvcTest(controllers = HealthController.class)
@ExtendWith({SpringExtension.class})
class HealthControllerTest {

    @MockBean
    HealthControlPanel controlPanel;

    MockMvc mockMvc;

    @Test
    void livenessProbeAlive() throws Exception {
        when(controlPanel.checkLiveness()).thenReturn(true);
        mockMvc.perform(get("/health/liveness"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("ALIVE"));
    }

    @Test
    void livenessProveNotAlive() throws Exception {
        when(controlPanel.checkLiveness()).thenReturn(false);
        mockMvc.perform(get("/health/liveness"))
                .andDo(print())
                .andExpect(status().is(HttpStatus.SERVICE_UNAVAILABLE.value()));
    }
}