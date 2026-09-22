package com.digitalfix.catalog.controller;

import com.digitalfix.catalog.model.dto.TechnicalServiceRequestDTO;
import com.digitalfix.catalog.model.entity.TechnicalService;
import com.digitalfix.catalog.service.TechnicalServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Define que esta clase es un controlador REST[cite: 1]
@RequestMapping("/api/catalog/services") // Exposición de la API solicitada
@RequiredArgsConstructor
public class TechnicalServiceController {

    private final TechnicalServiceService service;

    @PostMapping
    public ResponseEntity<TechnicalService> createService(@Valid @RequestBody TechnicalServiceRequestDTO request) {
        return new ResponseEntity<>(service.createService(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TechnicalService>> getAllServices() {
        return ResponseEntity.ok(service.getAllServices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TechnicalService> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getServiceById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TechnicalService> updateService(
            @PathVariable Long id,
            @Valid @RequestBody TechnicalServiceRequestDTO request) {
        return ResponseEntity.ok(service.updateService(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        service.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}

