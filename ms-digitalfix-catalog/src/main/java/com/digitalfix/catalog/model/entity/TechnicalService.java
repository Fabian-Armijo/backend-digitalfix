package com.digitalfix.catalog.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data // Genera getters, setters, toString, equals y hashCode automáticamente
@Builder // Permite construir objetos de forma fluida (Patrón Builder)
@NoArgsConstructor // Requerido por JPA[cite: 1]
@AllArgsConstructor // Requerido por el patrón Builder[cite: 1]
@Entity // Indica que esta clase es una tabla de base de datos[cite: 1]
@Table(name = "technical_services") // Nombre de la tabla en PostgreSQL[cite: 1]
public class TechnicalService {

    @Id // Define la clave primaria[cite: 1]
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental en PostgreSQL[cite: 1]
    private Long id;

    @Column(nullable = false, length = 100) // Configuración de la columna[cite: 1]
    private String name;

    @Column(nullable = false)
    private BigDecimal fee; // Usamos BigDecimal para manejar dinero/tarifas con precisión

    @Column(length = 500)
    private String description;
}