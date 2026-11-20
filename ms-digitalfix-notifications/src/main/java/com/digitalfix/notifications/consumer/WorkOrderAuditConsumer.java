package com.digitalfix.notifications.consumer;

import com.digitalfix.notifications.model.WorkOrderAuditEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Consumidor de Apache Kafka que registra la bitacora inmutable de auditoria
 * de todas las transacciones sobre ordenes de trabajo en DigitalFix.
 */
@Slf4j
@Component
public class WorkOrderAuditConsumer {

    private static final int MAX_AUDIT_HISTORY = 200;

    private final ObjectMapper objectMapper;
    private final Deque<WorkOrderAuditEvent> recentAuditEvents = new ConcurrentLinkedDeque<>();

    public WorkOrderAuditConsumer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @KafkaListener(
            topics = "${app.kafka.topic.audit:workorders.audit.events}",
            groupId = "${spring.kafka.consumer.group-id:digitalfix-audit-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAuditEvent(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        try {
            WorkOrderAuditEvent event = objectMapper.readValue(payload, WorkOrderAuditEvent.class);

            log.info("[Kafka Audit Consumer] Registro recibido | Topico: {} | Particion: {} | Offset: {} | Key: {} | Tipo: {} | Orden #{} ({} -> {}) | Cliente: {} | Tecnico: {} | Fecha: {}",
                    topic,
                    partition,
                    offset,
                    key,
                    event.getEventType(),
                    event.getOrderId(),
                    event.getPreviousStatus(),
                    event.getNewStatus(),
                    event.getCustomerId(),
                    event.getTechnicianId(),
                    event.getTimestamp());

            recentAuditEvents.addFirst(event);
            while (recentAuditEvents.size() > MAX_AUDIT_HISTORY) {
                recentAuditEvents.pollLast();
            }
        } catch (Exception e) {
            log.error("[Kafka Audit Consumer] Error deserializando mensaje de auditoria | Topico: {} | Particion: {} | Offset: {} | Error: {}",
                    topic, partition, offset, e.getMessage());
        }
    }

    /**
     * Retorna los ultimos eventos de auditoria consumidos desde Kafka (mas recientes primero).
     */
    public List<WorkOrderAuditEvent> getRecentAuditEvents() {
        return Collections.unmodifiableList(new ArrayList<>(recentAuditEvents));
    }
}
