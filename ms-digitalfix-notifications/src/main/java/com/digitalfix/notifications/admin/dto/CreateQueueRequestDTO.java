package com.digitalfix.notifications.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para la creacion programatica de colas en RabbitMQ.
 * 
 * Cumple con el Indicador 8 de la pauta de evaluacion:
 * Valida que los nombres no esten vacios, que no contengan caracteres invalidos
 * y que las configuraciones de durabilidad y alta disponibilidad sean validas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQueueRequestDTO {

    @NotBlank(message = "El nombre de la cola es obligatorio y no puede estar vacio")
    @Size(min = 3, max = 255, message = "El nombre de la cola debe tener entre 3 y 255 caracteres")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]+$",
        message = "El nombre de la cola solo puede contener caracteres alfanumericos, puntos, guiones y guiones bajos"
    )
    private String name;

    @Builder.Default
    private boolean durable = true;

    @Builder.Default
    private boolean exclusive = false;

    @Builder.Default
    private boolean autoDelete = false;

    /**
     * Si es true, configura la cola como Quorum Queue (Replicacion Raft en Cluster de alta disponibilidad).
     */
    @Builder.Default
    private boolean quorum = false;

    /**
     * Nombre opcional del Dead Letter Exchange para canalizar fallos.
     */
    private String deadLetterExchange;

    /**
     * Routing key opcional para el Dead Letter Exchange.
     */
    private String deadLetterRoutingKey;

    /**
     * Argumentos adicionales opcionales para la cola.
     */
    private Map<String, Object> additionalArguments;
}
