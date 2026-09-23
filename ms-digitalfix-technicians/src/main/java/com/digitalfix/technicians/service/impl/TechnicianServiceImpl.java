package com.digitalfix.technicians.service.impl;

import com.digitalfix.technicians.model.dto.TechnicianRequestDTO;
import com.digitalfix.technicians.model.entity.Technician;
import com.digitalfix.technicians.model.entity.TechnicianStatus;
import com.digitalfix.technicians.repository.TechnicianRepository;
import com.digitalfix.technicians.service.TechnicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TechnicianServiceImpl implements TechnicianService {

    private final TechnicianRepository repository;

    @Override
    public Technician createTechnician(TechnicianRequestDTO request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un técnico registrado con el correo: " + request.getEmail());
        }

        Technician technician = Technician.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .specialty(request.getSpecialty())
                .status(request.getStatus() != null ? request.getStatus() : TechnicianStatus.DISPONIBLE)
                .build();

        return repository.save(technician);
    }

    @Override
    public List<Technician> getAllTechnicians() {
        return repository.findAll();
    }

    @Override
    public Technician getTechnicianById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Técnico no encontrado con ID: " + id));
    }

    @Override
    public List<Technician> getTechniciansByStatus(TechnicianStatus status) {
        return repository.findByStatus(status);
    }

    @Override
    public List<Technician> getAvailableTechnicians() {
        return repository.findByStatus(TechnicianStatus.DISPONIBLE);
    }

    @Override
    public Technician updateTechnician(Long id, TechnicianRequestDTO request) {
        Technician technician = getTechnicianById(id);

        if (repository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe otro técnico registrado con el correo: " + request.getEmail());
        }

        technician.setFirstName(request.getFirstName());
        technician.setLastName(request.getLastName());
        technician.setEmail(request.getEmail());
        technician.setPhone(request.getPhone());
        technician.setSpecialty(request.getSpecialty());
        if (request.getStatus() != null) {
            technician.setStatus(request.getStatus());
        }

        return repository.save(technician);
    }

    @Override
    public Technician updateStatus(Long id, TechnicianStatus status) {
        Technician technician = getTechnicianById(id);
        technician.setStatus(status);
        return repository.save(technician);
    }

    @Override
    public void deleteTechnician(Long id) {
        Technician technician = getTechnicianById(id);
        repository.delete(technician);
    }
}
