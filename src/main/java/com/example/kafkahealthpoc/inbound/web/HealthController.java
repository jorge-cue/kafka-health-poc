package com.example.kafkahealthpoc.inbound.web;

import com.example.kafkahealthpoc.selfheal.HealthControlPanel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Created by jhcue on 21/03/2021
 */
@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {

    private final HealthControlPanel healthControlPanel;

    public HealthController(HealthControlPanel healthControlPanel) {
        this.healthControlPanel = healthControlPanel;
    }

    @GetMapping("/liveness")
    public ResponseEntity<String> livenessProbe() {
        return healthControlPanel.checkLiveness()
                ? ResponseEntity.ok("{\"status\":\"ALIVE\"}")
                : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @GetMapping("/readiness")
    public ResponseEntity<String> readinessProbe() {
        return healthControlPanel.checkReadiness()
                ? ResponseEntity.ok("{\"status\":\"READY\"}")
                : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
