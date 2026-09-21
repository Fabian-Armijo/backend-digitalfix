package com.digitalfix.workorders.repository;

import com.digitalfix.workorders.model.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    // Spring Data JPA provee save(), findById(), findAll(), deleteById() automáticamente.
    // Aquí después podremos agregar búsquedas personalizadas, por ejemplo:
    // List<WorkOrder> findByCustomerId(Long customerId);
}