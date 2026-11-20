package com.digitalfix.notifications.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Modelo de evento de auditoria consumido desde el topico de Apache Kafka.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderAuditEvent {
    private String eventId;
    private Long orderId;
    private Long customerId;
    private Long technicianId;
    private String eventType;
    private String previousStatus;
    private String newStatus;
    private String description;
    private String sourceService;
    private LocalDateTime timestamp;
}
