package com.digitalfix.workorders.service;

import com.digitalfix.workorders.model.event.WorkOrderAuditEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Productor Kafka encargado de emitir la bitacora inmutable de auditoria
 * de ordenes de trabajo hacia el topico workorders.audit.events.
 */
@Slf4j
@Service
public class KafkaAuditProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.audit:workorders.audit.events}")
    private String auditTopic;

    public KafkaAuditProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Publica un evento de auditoria en Kafka de forma asincrona.
     * Utiliza el orderId como clave de particion para garantizar el orden cronologico
     * de todos los eventos pertenecientes a una misma orden de trabajo.
     */
    public void publishAuditEvent(WorkOrderAuditEvent event) {
        try {
            if (event.getEventId() == null) {
                event.setEventId(UUID.randomUUID().toString());
            }
            if (event.getSourceService() == null) {
                event.setSourceService("ms-digitalfixworkorders");
            }

            String key = event.getOrderId() != null ? String.valueOf(event.getOrderId()) : event.getEventId();
            String payload = objectMapper.writeValueAsString(event);

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(auditTopic, key, payload);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[Kafka Audit] Evento '{}' publicado | Topico: {} | Particion: {} | Offset: {} | Orden #{}",
                            event.getEventType(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            event.getOrderId());
                } else {
                    log.error("[Kafka Audit] Error al publicar evento '{}' para Orden #{}: {}",
                            event.getEventType(), event.getOrderId(), ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("[Kafka Audit] Error serializando evento de auditoria para Orden #{}: {}",
                    event.getOrderId(), e.getMessage());
        }
    }

    /**
     * Envia un mensaje crudo a un topico especifico (usado para pruebas de estres y benchmarks).
     */
    public CompletableFuture<SendResult<String, String>> sendRawEvent(String topic, String key, String payload) {
        return kafkaTemplate.send(topic, key, payload);
    }
}
