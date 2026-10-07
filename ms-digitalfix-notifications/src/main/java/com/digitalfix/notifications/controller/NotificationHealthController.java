package com.digitalfix.notifications.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationHealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        return ResponseEntity.ok(Map.of(
                "service", "ms-digitalfix-notifications",
                "status", "UP",
                "broker", "RabbitMQ Cluster",
                "queue", "workorders.notifications.queue",
                "dlq", "workorders.notifications.dlq",
                "timestamp", LocalDateTime.now()
        ));
    }
}
