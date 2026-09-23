package com.digitalfix.technicians.controller;

import com.digitalfix.technicians.model.dto.TechnicianRequestDTO;
import com.digitalfix.technicians.model.entity.Technician;
import com.digitalfix.technicians.model.entity.TechnicianStatus;
import com.digitalfix.technicians.service.TechnicianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianService service;

    @PostMapping
    public ResponseEntity<Technician> createTechnician(@Valid @RequestBody TechnicianRequestDTO request) {
        return new ResponseEntity<>(service.createTechnician(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Technician>> getAllTechnicians(
            @RequestParam(required = false) TechnicianStatus status) {
        if (status != null) {
            return ResponseEntity.ok(service.getTechniciansByStatus(status));
        }
        return ResponseEntity.ok(service.getAllTechnicians());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Technician> getTechnicianById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTechnicianById(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Technician>> getAvailableTechnicians() {
        return ResponseEntity.ok(service.getAvailableTechnicians());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Technician> updateTechnician(
            @PathVariable Long id,
            @Valid @RequestBody TechnicianRequestDTO request) {
        return ResponseEntity.ok(service.updateTechnician(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Technician> updateStatus(
            @PathVariable Long id,
            @RequestParam TechnicianStatus status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTechnician(@PathVariable Long id) {
        service.deleteTechnician(id);
        return ResponseEntity.noContent().build();
    }
}
