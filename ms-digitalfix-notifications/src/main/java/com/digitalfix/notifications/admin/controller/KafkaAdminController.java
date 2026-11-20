package com.digitalfix.notifications.admin.controller;

import com.digitalfix.notifications.admin.dto.AdminOperationResponseDTO;
import com.digitalfix.notifications.admin.dto.CreateTopicRequestDTO;
import com.digitalfix.notifications.admin.dto.TopicInfoDTO;
import com.digitalfix.notifications.admin.dto.UpdateTopicRequestDTO;
import com.digitalfix.notifications.admin.service.KafkaAdminService;
import com.digitalfix.notifications.consumer.WorkOrderAuditConsumer;
import com.digitalfix.notifications.model.WorkOrderAuditEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la administracion de topicos y configuraciones de Apache Kafka.
 *
 * Endpoints expuestos:
 * - POST   /api/admin/kafka/topics          : Crear topico con particiones, replicas y configuraciones
 * - GET    /api/admin/kafka/topics          : Listar topicos del cluster
 * - GET    /api/admin/kafka/topics/{name}   : Consultar detalle y configuraciones de un topico
 * - PUT    /api/admin/kafka/topics/{name}   : Modificar particiones y/o configuraciones de un topico
 * - DELETE /api/admin/kafka/topics/{name}   : Eliminar topico del cluster
 * - GET    /api/admin/kafka/audit-logs      : Consultar ultimos registros de auditoria consumidos desde Kafka
 */
@RestController
@RequestMapping("/api/admin/kafka")
@RequiredArgsConstructor
public class KafkaAdminController {

    private final KafkaAdminService kafkaAdminService;
    private final WorkOrderAuditConsumer auditConsumer;

    @PostMapping("/topics")
    public ResponseEntity<AdminOperationResponseDTO> createTopic(
            @Valid @RequestBody CreateTopicRequestDTO request) {
        AdminOperationResponseDTO response = kafkaAdminService.createTopic(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/topics")
    public ResponseEntity<List<String>> listTopics(
            @RequestParam(defaultValue = "false") boolean includeInternal) {
        return ResponseEntity.ok(kafkaAdminService.listTopics(includeInternal));
    }

    @GetMapping("/topics/{name}")
    public ResponseEntity<TopicInfoDTO> getTopicInfo(
            @PathVariable("name") String name) {
        return ResponseEntity.ok(kafkaAdminService.getTopicInfo(name));
    }

    @PutMapping("/topics/{name}")
    public ResponseEntity<AdminOperationResponseDTO> updateTopic(
            @PathVariable("name") String name,
            @Valid @RequestBody UpdateTopicRequestDTO request) {
        return ResponseEntity.ok(kafkaAdminService.updateTopic(name, request));
    }

    @DeleteMapping("/topics/{name}")
    public ResponseEntity<AdminOperationResponseDTO> deleteTopic(
            @PathVariable("name") String name) {
        return ResponseEntity.ok(kafkaAdminService.deleteTopic(name));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<WorkOrderAuditEvent>> getRecentAuditLogs() {
        return ResponseEntity.ok(auditConsumer.getRecentAuditEvents());
    }
}
