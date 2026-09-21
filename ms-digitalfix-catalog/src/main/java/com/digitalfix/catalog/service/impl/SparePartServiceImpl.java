package com.digitalfix.catalog.service.impl;

import com.digitalfix.catalog.model.dto.SparePartRequestDTO;
import com.digitalfix.catalog.model.entity.SparePart;
import com.digitalfix.catalog.repository.SparePartRepository;
import com.digitalfix.catalog.service.SparePartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SparePartServiceImpl implements SparePartService {

    private final SparePartRepository repository;

    @Override
    public SparePart createSparePart(SparePartRequestDTO request) {
        SparePart part = SparePart.builder()
                .name(request.getName())
                .cost(request.getCost())
                .stock(request.getStock())
                .build();
        return repository.save(part);
    }

    @Override
    public List<SparePart> getAllSpareParts() {
        return repository.findAll();
    }

    @Override
    public SparePart getSparePartById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repuesto no encontrado"));
    }

    // REGLA CLAVE: El stock disminuye al asignar la orden
    @Override
    public SparePart reduceStock(Long id, Integer quantity) {
        SparePart part = getSparePartById(id);

        if (part.getStock() < quantity) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para el repuesto seleccionado"); // Evita que el inventario quede en negativo[cite: 1]
        }

        part.setStock(part.getStock() - quantity);
        return repository.save(part);
    }
}
