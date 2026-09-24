package com.digitalfix.workorders.service.impl;

import com.digitalfix.workorders.model.dto.WorkOrderRequestDTO;
import com.digitalfix.workorders.model.entity.WorkOrder;
import com.digitalfix.workorders.model.entity.WorkOrderStatus;
import com.digitalfix.workorders.repository.WorkOrderRepository;
import com.digitalfix.workorders.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor // Lombok inyecta el repositorio automáticamente mediante el constructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository repository;

    @Override
    public WorkOrder createWorkOrder(WorkOrderRequestDTO request) {
        WorkOrder workOrder = WorkOrder.builder()
                .customerId(request.getCustomerId())
                .description(request.getDescription())
                // El estado CREADA y la fecha se asignan solos por el @PrePersist de la Entidad
                .build();
        return repository.save(workOrder);
    }

    @Override
    public List<WorkOrder> getAllWorkOrders() {
        return repository.findAll();
    }

    @Override
    public WorkOrder getWorkOrderById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden de trabajo no encontrada"));
    }

    @Override
    public WorkOrder assignTechnician(Long orderId, Long technicianId) {
        WorkOrder order = getWorkOrderById(orderId);

        if (order.getStatus() != WorkOrderStatus.CREADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden asignar órdenes en estado CREADA");
        }

        order.setTechnicianId(technicianId);
        order.setStatus(WorkOrderStatus.ASIGNADA);
        return repository.save(order);
    }

    @Override
    public WorkOrder updateStatus(Long orderId, WorkOrderStatus newStatus) {
        WorkOrder order = getWorkOrderById(orderId);
        WorkOrderStatus currentStatus = order.getStatus();

        // REGLA CLAVE DE NEGOCIO: Validar transición a EN_EJECUCION
        if (newStatus == WorkOrderStatus.EN_EJECUCION) {
            if (currentStatus != WorkOrderStatus.ASIGNADA && currentStatus != WorkOrderStatus.EN_DESPLAZAMIENTO) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Regla de negocio violada: No se puede pasar a EN_EJECUCIÓN sin haber sido ASIGNADA previamente.");
            }
            if (order.getTechnicianId() == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Regla de negocio violada: La orden no tiene un técnico asignado.");
            }
        }

        order.setStatus(newStatus);
        return repository.save(order);
    }

    @Override
    public void deleteWorkOrder(Long id) {
        WorkOrder order = getWorkOrderById(id);
        repository.delete(order);
    }
}
