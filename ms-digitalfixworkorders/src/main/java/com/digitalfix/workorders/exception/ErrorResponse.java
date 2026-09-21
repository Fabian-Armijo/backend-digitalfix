package com.digitalfix.workorders.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Si un campo es nulo, no se muestra en el JSON
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status; // Ejemplo: 400, 404, 409
    private String error; // Ejemplo: "Bad Request", "Conflict"
    private String message; // Tu mensaje personalizado
    private List<String> details; // Detalles adicionales (ej. lista de campos mal validados)
}