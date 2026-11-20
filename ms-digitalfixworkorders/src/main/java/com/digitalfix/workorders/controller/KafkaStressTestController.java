package com.digitalfix.workorders.controller;

import com.digitalfix.workorders.service.KafkaAuditProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controlador para pruebas de estres y medicion de rendimiento del productor optimizado de Kafka.
 */
@Slf4j
@RestController
@RequestMapping("/api/workorders/kafka")
@RequiredArgsConstructor
public class KafkaStressTestController {

    private final KafkaAuditProducer kafkaAuditProducer;

    @Value("${app.kafka.topic.stress:workorders.stress.test}")
    private String defaultStressTopic;

    /**
     * Envia una rafaga de mensajes a un topico anticipado de Kafka y mide latencia y throughput.
     * Ejemplo: POST /api/workorders/kafka/stress-test?count=1000&topic=workorders.stress.test
     */
    @PostMapping("/stress-test")
    public ResponseEntity<Map<String, Object>> runStressTest(
            @RequestParam(defaultValue = "1000") int count,
            @RequestParam(required = false) String topic) {

        int messageCount = Math.min(Math.max(count, 1), 50000);
        String targetTopic = (topic != null && !topic.isBlank()) ? topic : defaultStressTopic;

        log.info("[Kafka Stress Test] Iniciando envio de {} mensajes al topico '{}'", messageCount, targetTopic);

        long startNanos = System.nanoTime();
        List<CompletableFuture<SendResult<String, String>>> futures = new ArrayList<>(messageCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 1; i <= messageCount; i++) {
            String key = "order-stress-" + (i % 100);
            String payload = String.format(
                    "{\"eventId\":\"stress-%d\",\"orderId\":%d,\"eventType\":\"STRESS_TEST\",\"newStatus\":\"TESTING\",\"description\":\"Mensaje de prueba de estres #%d\",\"sourceService\":\"ms-digitalfixworkorders\",\"timestamp\":\"%s\"}",
                    i, i, i, LocalDateTime.now()
            );

            CompletableFuture<SendResult<String, String>> future = kafkaAuditProducer.sendRawEvent(targetTopic, key, payload);
            future.whenComplete((res, ex) -> {
                if (ex == null) {
                    successCount.incrementAndGet();
                } else {
                    errorCount.incrementAndGet();
                }
            });
            futures.add(future);
        }

        // Esperar confirmacion del broker para todos los lotes enviados
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("[Kafka Stress Test] Finalizo con advertencia/timeout: {}", e.getMessage());
        }

        long elapsedNanos = System.nanoTime() - startNanos;
        double totalMs = elapsedNanos / 1_000_000.0;
        double avgLatencyMs = totalMs / messageCount;
        double throughputMsgPerSec = totalMs > 0 ? (successCount.get() / (totalMs / 1000.0)) : 0;

        log.info("[Kafka Stress Test] Completado: {} exitosos, {} fallidos en {} ms ({} msgs/sec)",
                successCount.get(), errorCount.get(), String.format("%.2f", totalMs), String.format("%.2f", throughputMsgPerSec));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("topic", targetTopic);
        report.put("requestedMessages", messageCount);
        report.put("successMessages", successCount.get());
        report.put("failedMessages", errorCount.get());
        report.put("totalTimeMs", Math.round(totalMs * 100.0) / 100.0);
        report.put("avgTimePerMessageMs", Math.round(avgLatencyMs * 10000.0) / 10000.0);
        report.put("throughputMsgPerSec", Math.round(throughputMsgPerSec * 100.0) / 100.0);
        report.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(report);
    }
}
