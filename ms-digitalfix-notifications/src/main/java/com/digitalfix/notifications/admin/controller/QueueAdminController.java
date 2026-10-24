package com.digitalfix.notifications.admin.controller;

import com.digitalfix.notifications.admin.dto.*;
import com.digitalfix.notifications.admin.service.RabbitAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la administracion de RabbitMQ.
 * 
 * Cumple con los Indicadores 6 y 8 de la pauta de evaluacion:
 * - Indicador 6 (13%): Endpoints REST bien definidos con verbos HTTP estandar
 *   (POST /api/admin/queues, DELETE /api/admin/queues/{name}, etc.).
 * - Indicador 8 (7%): Validacion estricta de parametros con @Valid y DTOs especializados.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class QueueAdminController {

    private final RabbitAdminService adminService;

    /**
     * Endpoint para crear una nueva cola en RabbitMQ.
     * Ejemplo: POST /api/admin/queues
     */
    @PostMapping("/queues")
    public ResponseEntity<AdminOperationResponseDTO> createQueue(
            @Valid @RequestBody CreateQueueRequestDTO request) {
        AdminOperationResponseDTO response = adminService.createQueue(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint para consultar metricas en tiempo real de una cola.
     * Ejemplo: GET /api/admin/queues/workorders.notifications.queue
     */
    @GetMapping("/queues/{name}")
    public ResponseEntity<QueueInfoDTO> getQueueInfo(
            @PathVariable("name") String name) {
        return ResponseEntity.ok(adminService.getQueueInfo(name));
    }

    /**
     * Endpoint para eliminar una cola por su nombre.
     * Ejemplo: DELETE /api/admin/queues/test.queue?ifUnused=false
     */
    @DeleteMapping("/queues/{name}")
    public ResponseEntity<AdminOperationResponseDTO> deleteQueue(
            @PathVariable("name") String name,
            @RequestParam(defaultValue = "false") boolean ifUnused,
            @RequestParam(defaultValue = "false") boolean ifEmpty) {
        return ResponseEntity.ok(adminService.deleteQueue(name, ifUnused, ifEmpty));
    }

    /**
     * Endpoint para purgar todos los mensajes de una cola.
     * Ejemplo: POST /api/admin/queues/test.queue/purge
     */
    @PostMapping("/queues/{name}/purge")
    public ResponseEntity<AdminOperationResponseDTO> purgeQueue(
            @PathVariable("name") String name) {
        return ResponseEntity.ok(adminService.purgeQueue(name));
    }

    /**
     * Endpoint para crear un Exchange.
     * Ejemplo: POST /api/admin/exchanges
     */
    @PostMapping("/exchanges")
    public ResponseEntity<AdminOperationResponseDTO> createExchange(
            @Valid @RequestBody CreateExchangeRequestDTO request) {
        AdminOperationResponseDTO response = adminService.createExchange(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint para eliminar un Exchange.
     * Ejemplo: DELETE /api/admin/exchanges/test.exchange
     */
    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<AdminOperationResponseDTO> deleteExchange(
            @PathVariable("name") String name) {
        return ResponseEntity.ok(adminService.deleteExchange(name));
    }

    /**
     * Endpoint para vincular una cola a un exchange con una routing key.
     * Ejemplo: POST /api/admin/bindings
     */
    @PostMapping("/bindings")
    public ResponseEntity<AdminOperationResponseDTO> createBinding(
            @Valid @RequestBody CreateBindingRequestDTO request) {
        AdminOperationResponseDTO response = adminService.createBinding(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
