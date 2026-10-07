package com.digitalfix.workorders.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Long customerId;
    private Long technicianId;
    private String status;
    private String previousStatus;
    private String description;
    private String eventType; // "ORDER_CREATED", "STATUS_UPDATED", "TECHNICIAN_ASSIGNED"
    private LocalDateTime timestamp;
}
