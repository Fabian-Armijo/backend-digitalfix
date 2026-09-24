package com.digitalfix.workorders.controller;

import com.digitalfix.workorders.model.dto.WorkOrderRequestDTO;
import com.digitalfix.workorders.model.entity.WorkOrder;
import com.digitalfix.workorders.model.entity.WorkOrderStatus;
import com.digitalfix.workorders.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workorders") // La ruta base requerida
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService service;

    // POST /api/workorders (El Cliente crea la orden)
    @PostMapping
    public ResponseEntity<WorkOrder> createWorkOrder(@Valid @RequestBody WorkOrderRequestDTO request) {
        return new ResponseEntity<>(service.createWorkOrder(request), HttpStatus.CREATED);
    }

    // GET /api/workorders (Para Admin o Supervisor)
    @GetMapping
    public ResponseEntity<List<WorkOrder>> getAllWorkOrders() {
        return ResponseEntity.ok(service.getAllWorkOrders());
    }

    // GET /api/workorders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<WorkOrder> getWorkOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getWorkOrderById(id));
    }

    // PATCH /api/workorders/{id}/assign/{technicianId} (El Supervisor asigna al Técnico)
    @PatchMapping("/{id}/assign/{technicianId}")
    public ResponseEntity<WorkOrder> assignTechnician(@PathVariable Long id, @PathVariable Long technicianId) {
        return ResponseEntity.ok(service.assignTechnician(id, technicianId));
    }

    // PATCH /api/workorders/{id}/status?newStatus=EN_EJECUCION (El Técnico cambia de estado)
    @PatchMapping("/{id}/status")
    public ResponseEntity<WorkOrder> updateStatus(@PathVariable Long id, @RequestParam WorkOrderStatus newStatus) {
        return ResponseEntity.ok(service.updateStatus(id, newStatus));
    }

    // DELETE /api/workorders/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkOrder(@PathVariable Long id) {
        service.deleteWorkOrder(id);
        return ResponseEntity.noContent().build();
    }
}
