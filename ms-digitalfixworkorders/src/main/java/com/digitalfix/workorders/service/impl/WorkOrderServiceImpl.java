package com.digitalfix.workorders.service.impl;

import com.digitalfix.workorders.config.RabbitMQConfig;
import com.digitalfix.workorders.model.dto.WorkOrderRequestDTO;
import com.digitalfix.workorders.model.entity.WorkOrder;
import com.digitalfix.workorders.model.entity.WorkOrderStatus;
import com.digitalfix.workorders.model.event.WorkOrderEvent;
import com.digitalfix.workorders.repository.WorkOrderRepository;
import com.digitalfix.workorders.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository repository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public WorkOrder createWorkOrder(WorkOrderRequestDTO request) {
        WorkOrder workOrder = WorkOrder.builder()
                .customerId(request.getCustomerId())
                .description(request.getDescription())
                // El estado CREADA y la fecha se asignan solos por el @PrePersist de la Entidad
                .build();
        WorkOrder saved = repository.save(workOrder);

        // Publicación asíncrona del evento al cluster de RabbitMQ
        publishEvent(WorkOrderEvent.builder()
                .orderId(saved.getId())
                .customerId(saved.getCustomerId())
                .technicianId(saved.getTechnicianId())
                .status(saved.getStatus().name())
                .previousStatus(null)
                .description(saved.getDescription())
                .eventType("ORDER_CREATED")
                .timestamp(LocalDateTime.now())
                .build(), RabbitMQConfig.ROUTING_KEY_CREATED);

        return saved;
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
        WorkOrderStatus prevStatus = order.getStatus();

        if (order.getStatus() != WorkOrderStatus.CREADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden asignar órdenes en estado CREADA");
        }

        order.setTechnicianId(technicianId);
        order.setStatus(WorkOrderStatus.ASIGNADA);
        WorkOrder saved = repository.save(order);

        // Notificación asíncrona de asignación de técnico
        publishEvent(WorkOrderEvent.builder()
                .orderId(saved.getId())
                .customerId(saved.getCustomerId())
                .technicianId(saved.getTechnicianId())
                .status(saved.getStatus().name())
                .previousStatus(prevStatus.name())
                .description(saved.getDescription())
                .eventType("TECHNICIAN_ASSIGNED")
                .timestamp(LocalDateTime.now())
                .build(), RabbitMQConfig.ROUTING_KEY_ASSIGNED);

        return saved;
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
        WorkOrder saved = repository.save(order);

        // Notificación asíncrona de cambio de estado (ej: EN_DESPLAZAMIENTO, EN_EJECUCION, CERRADA)
        publishEvent(WorkOrderEvent.builder()
                .orderId(saved.getId())
                .customerId(saved.getCustomerId())
                .technicianId(saved.getTechnicianId())
                .status(saved.getStatus().name())
                .previousStatus(currentStatus.name())
                .description(saved.getDescription())
                .eventType("STATUS_UPDATED")
                .timestamp(LocalDateTime.now())
                .build(), RabbitMQConfig.ROUTING_KEY_STATUS_CHANGED);

        return saved;
    }

    @Override
    public void deleteWorkOrder(Long id) {
        WorkOrder order = getWorkOrderById(id);
        repository.delete(order);
    }

    /**
     * Emite el evento al TopicExchange de RabbitMQ sin bloquear la transacción principal
     */
    private void publishEvent(WorkOrderEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
            log.info("🚀 [RabbitMQ Cluster] Evento emitido a Exchange '{}' | RoutingKey '{}' | Orden #{} -> {}",
                    RabbitMQConfig.EXCHANGE_NAME, routingKey, event.getOrderId(), event.getStatus());
        } catch (Exception e) {
            log.error("⚠️ [RabbitMQ Cluster] Advertencia: No se pudo emitir el evento para la orden #{}: {}",
                    event.getOrderId(), e.getMessage());
        }
    }
}
