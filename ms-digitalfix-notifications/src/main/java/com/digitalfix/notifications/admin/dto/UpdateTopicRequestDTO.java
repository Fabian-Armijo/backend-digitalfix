package com.digitalfix.notifications.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO de solicitud para modificar un topico existente en Apache Kafka
 * (incrementar particiones y/o alterar configuraciones de retencion/compresion).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTopicRequestDTO {

    /**
     * Nuevo numero total de particiones (en Kafka solo se puede incrementar, nunca disminuir).
     */
    @Min(value = 1, message = "El numero de particiones debe ser al menos 1")
    @Max(value = 100, message = "El numero de particiones no puede superar 100")
    private Integer newPartitions;

    /**
     * Mapa de configuraciones a modificar dinamicamente en el topico
     * (ej: retention.ms, retention.bytes, cleanup.policy, max.message.bytes).
     */
    private Map<String, String> configs;
}
