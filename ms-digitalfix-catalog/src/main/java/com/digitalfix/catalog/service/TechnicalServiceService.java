package com.digitalfix.catalog.service;

import com.digitalfix.catalog.model.dto.TechnicalServiceRequestDTO;
import com.digitalfix.catalog.model.entity.TechnicalService;
import java.util.List;

public interface TechnicalServiceService {
    TechnicalService createService(TechnicalServiceRequestDTO request);
    List<TechnicalService> getAllServices();
    TechnicalService getServiceById(Long id);
    TechnicalService updateService(Long id, TechnicalServiceRequestDTO request);
    void deleteService(Long id);
}