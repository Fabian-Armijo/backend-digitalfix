package com.digitalfix.notifications.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Respuesta estandarizada para operaciones administrativas sobre RabbitMQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOperationResponseDTO {
    private String status; // "SUCCESS", "ERROR"
    private String message;
    private String resourceType; // "QUEUE", "EXCHANGE", "BINDING"
    private String resourceName;
    private LocalDateTime timestamp;
}
