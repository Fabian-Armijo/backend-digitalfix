package com.digitalfix.workorders.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Evento inmutable de auditoria y trazabilidad de ordenes de trabajo publicado en Apache Kafka.
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
    private String eventType;      // ORDER_CREATED, TECHNICIAN_ASSIGNED, STATUS_UPDATED, ORDER_UPDATED, ORDER_DELETED
    private String previousStatus;
    private String newStatus;
    private String description;
    private String sourceService;
    private LocalDateTime timestamp;
}
