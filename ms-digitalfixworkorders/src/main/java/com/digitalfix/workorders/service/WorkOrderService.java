package com.digitalfix.workorders.service;

import com.digitalfix.workorders.model.dto.WorkOrderRequestDTO;
import com.digitalfix.workorders.model.entity.WorkOrder;
import com.digitalfix.workorders.model.entity.WorkOrderStatus;
import java.util.List;

public interface WorkOrderService {
    WorkOrder createWorkOrder(WorkOrderRequestDTO request);
    List<WorkOrder> getAllWorkOrders();
    WorkOrder getWorkOrderById(Long id);
    WorkOrder assignTechnician(Long orderId, Long technicianId); // Acción del Supervisor
    WorkOrder updateStatus(Long orderId, WorkOrderStatus newStatus); // Cambio de estados
    WorkOrder updateWorkOrder(Long id, WorkOrderRequestDTO request); // Edición de orden
    void deleteWorkOrder(Long id);
}
