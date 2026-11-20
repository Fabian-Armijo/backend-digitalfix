package com.digitalfix.notifications.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO de solicitud para crear un nuevo topico en Apache Kafka.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTopicRequestDTO {

    @NotBlank(message = "El nombre del topico es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre del topico solo puede contener letras, numeros, puntos, guiones y guiones bajos")
    private String name;

    @Min(value = 1, message = "El numero de particiones debe ser al menos 1")
    @Max(value = 100, message = "El numero de particiones no puede superar 100")
    @Builder.Default
    private int partitions = 3;

    @Min(value = 1, message = "El factor de replicacion debe ser al menos 1")
    @Max(value = 10, message = "El factor de replicacion no puede superar 10")
    @Builder.Default
    private short replicationFactor = 2;

    /**
     * Configuraciones opcionales del topico (ej: retention.ms, retention.bytes, cleanup.policy, compression.type).
     */
    private Map<String, String> configs;
}
