package com.digitalfix.technicians.service;

import com.digitalfix.technicians.model.dto.TechnicianRequestDTO;
import com.digitalfix.technicians.model.entity.Technician;
import com.digitalfix.technicians.model.entity.TechnicianStatus;

import java.util.List;

public interface TechnicianService {

    Technician createTechnician(TechnicianRequestDTO request);

    List<Technician> getAllTechnicians();

    Technician getTechnicianById(Long id);

    List<Technician> getTechniciansByStatus(TechnicianStatus status);

    List<Technician> getAvailableTechnicians();

    Technician updateTechnician(Long id, TechnicianRequestDTO request);

    Technician updateStatus(Long id, TechnicianStatus status);

    void deleteTechnician(Long id);
}
