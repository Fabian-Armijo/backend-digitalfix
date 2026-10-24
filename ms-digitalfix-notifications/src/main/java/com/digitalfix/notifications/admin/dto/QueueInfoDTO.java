package com.digitalfix.notifications.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO que expone informacion y metricas de una cola consultada en RabbitMQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueInfoDTO {
    private String name;
    private Integer messageCount;
    private Integer consumerCount;
    private boolean durable;
    private boolean exclusive;
    private boolean autoDelete;
    private Map<String, Object> arguments;
    private String status;
}
