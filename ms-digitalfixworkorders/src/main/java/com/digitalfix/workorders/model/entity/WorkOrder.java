package com.digitalfix.workorders.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data // Genera getters, setters, toString, equals y hashCode automáticamente
@Builder // Permite construir objetos de forma fluida (Patrón Builder)
@NoArgsConstructor // Requerido por JPA
@AllArgsConstructor // Requerido por el patrón Builder
@Entity // Indica que esta clase es una tabla de base de datos
@Table(name = "work_orders") // Nombre de la tabla en PostgreSQL
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Guardaremos el ID del cliente que crea la orden (Actor: Cliente)
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // Guardaremos el ID del técnico cuando el Supervisor lo asigne
    @Column(name = "technician_id")
    private Long technicianId;

    @Column(nullable = false, length = 500)
    private String description;

    // Guarda el estado como texto (ej: "CREADA") en vez de un número (0, 1) en PostgreSQL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Métodos de ciclo de vida de JPA para gestionar las fechas automáticamente
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // Cuando el Cliente crea la orden, siempre nace como CREADA
        if (this.status == null) {
            this.status = WorkOrderStatus.CREADA;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}