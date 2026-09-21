package com.digitalfix.workorders.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkOrderRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long customerId;

    @NotBlank(message = "La descripción del problema no puede estar vacía")
    private String description;
}