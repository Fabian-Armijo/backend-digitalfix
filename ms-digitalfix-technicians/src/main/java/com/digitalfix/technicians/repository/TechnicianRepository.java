package com.digitalfix.technicians.repository;

import com.digitalfix.technicians.model.entity.Technician;
import com.digitalfix.technicians.model.entity.TechnicianStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {

    List<Technician> findByStatus(TechnicianStatus status);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
