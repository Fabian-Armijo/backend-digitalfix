package com.digitalfix.notifications.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la vinculacion (Binding) entre una cola y un exchange.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBindingRequestDTO {

    @NotBlank(message = "El nombre de la cola es obligatorio")
    private String queueName;

    @NotBlank(message = "El nombre del exchange es obligatorio")
    private String exchangeName;

    @NotBlank(message = "La routing key de vinculacion es obligatoria")
    private String routingKey;
}
