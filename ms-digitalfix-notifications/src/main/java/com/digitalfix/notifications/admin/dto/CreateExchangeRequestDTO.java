package com.digitalfix.notifications.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creacion programatica de Exchanges en RabbitMQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExchangeRequestDTO {

    @NotBlank(message = "El nombre del exchange es obligatorio")
    @Size(min = 3, max = 255, message = "El nombre del exchange debe tener entre 3 y 255 caracteres")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]+$",
        message = "El nombre del exchange solo puede contener caracteres alfanumericos, puntos, guiones y guiones bajos"
    )
    private String name;

    @NotBlank(message = "El tipo de exchange es obligatorio (topic, direct, fanout, headers)")
    @Pattern(
        regexp = "^(?i)(topic|direct|fanout|headers)$",
        message = "Tipo de exchange invalido. Valores permitidos: topic, direct, fanout, headers"
    )
    private String type;

    @Builder.Default
    private Boolean durable = true;

    @Builder.Default
    private Boolean autoDelete = false;

    public boolean isDurable() {
        return durable == null || durable;
    }

    public boolean isAutoDelete() {
        return autoDelete != null && autoDelete;
    }
}
