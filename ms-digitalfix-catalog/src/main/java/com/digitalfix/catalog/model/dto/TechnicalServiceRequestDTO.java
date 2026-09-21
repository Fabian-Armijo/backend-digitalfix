package com.digitalfix.catalog.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TechnicalServiceRequestDTO {

    @NotBlank(message = "El nombre del servicio es obligatorio") // Valida que no esté vacío
    private String name;

    @NotNull(message = "La tarifa es obligatoria") // Valida nulos
    @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa debe ser mayor a 0")
    private BigDecimal fee;

    private String description; // Opcional
}
