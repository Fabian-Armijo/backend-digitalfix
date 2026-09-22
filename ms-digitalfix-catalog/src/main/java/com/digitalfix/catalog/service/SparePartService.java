package com.digitalfix.catalog.service;

import com.digitalfix.catalog.model.dto.SparePartRequestDTO;
import com.digitalfix.catalog.model.entity.SparePart;
import java.util.List;

public interface SparePartService {
    SparePart createSparePart(SparePartRequestDTO request);
    List<SparePart> getAllSpareParts();
    SparePart getSparePartById(Long id);
    SparePart reduceStock(Long id, Integer quantity); // Regla clave del negocio
    SparePart updateSparePart(Long id, SparePartRequestDTO request);
    void deleteSparePart(Long id);
}

