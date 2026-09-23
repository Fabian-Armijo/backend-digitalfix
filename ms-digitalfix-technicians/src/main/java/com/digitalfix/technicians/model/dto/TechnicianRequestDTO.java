package com.digitalfix.technicians.model.dto;

import com.digitalfix.technicians.model.entity.TechnicianStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TechnicianRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe ser válido")
    private String email;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    private String phone;

    @NotBlank(message = "La especialidad es obligatoria")
    private String specialty;

    private TechnicianStatus status;
}
