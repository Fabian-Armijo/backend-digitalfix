package com.digitalfix.catalog.repository;

import com.digitalfix.catalog.model.entity.TechnicalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnicalServiceRepository extends JpaRepository<TechnicalService, Long> {
}
