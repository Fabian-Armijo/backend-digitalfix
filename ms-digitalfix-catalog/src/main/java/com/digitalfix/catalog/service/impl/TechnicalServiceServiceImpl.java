package com.digitalfix.catalog.service.impl;

import com.digitalfix.catalog.model.dto.TechnicalServiceRequestDTO;
import com.digitalfix.catalog.model.entity.TechnicalService;
import com.digitalfix.catalog.repository.TechnicalServiceRepository;
import com.digitalfix.catalog.service.TechnicalServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor // Inyecta el repositorio automáticamente[cite: 1]
public class TechnicalServiceServiceImpl implements TechnicalServiceService {

    private final TechnicalServiceRepository repository;

    @Override
    public TechnicalService createService(TechnicalServiceRequestDTO request) {
        TechnicalService service = TechnicalService.builder()
                .name(request.getName())
                .fee(request.getFee())
                .description(request.getDescription())
                .build();
        return repository.save(service);
    }

    @Override
    public List<TechnicalService> getAllServices() {
        return repository.findAll();
    }

    @Override
    public TechnicalService getServiceById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio técnico no encontrado")); // Manejo centralizado de errores[cite: 1]
    }
}
