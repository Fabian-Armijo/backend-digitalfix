package com.digitalfix.catalog.controller;

import com.digitalfix.catalog.model.dto.SparePartRequestDTO;
import com.digitalfix.catalog.model.entity.SparePart;
import com.digitalfix.catalog.service.SparePartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/spare-parts") // Exposición de la API solicitada
@RequiredArgsConstructor
public class SparePartController {

    private final SparePartService service;

    @PostMapping
    public ResponseEntity<SparePart> createSparePart(@Valid @RequestBody SparePartRequestDTO request) {
        return new ResponseEntity<>(service.createSparePart(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SparePart>> getAllSpareParts() {
        return ResponseEntity.ok(service.getAllSpareParts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SparePart> getSparePartById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getSparePartById(id));
    }

    // Endpoint crucial: Para disminuir el stock cuando se asigne un repuesto a una orden
    @PatchMapping("/{id}/reduce-stock")
    public ResponseEntity<SparePart> reduceStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(service.reduceStock(id, quantity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SparePart> updateSparePart(
            @PathVariable Long id,
            @Valid @RequestBody SparePartRequestDTO request) {
        return ResponseEntity.ok(service.updateSparePart(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSparePart(@PathVariable Long id) {
        service.deleteSparePart(id);
        return ResponseEntity.noContent().build();
    }
}

