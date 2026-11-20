package com.digitalfix.notifications.admin.service;

import com.digitalfix.notifications.admin.dto.AdminOperationResponseDTO;
import com.digitalfix.notifications.admin.dto.CreateTopicRequestDTO;
import com.digitalfix.notifications.admin.dto.TopicInfoDTO;
import com.digitalfix.notifications.admin.dto.UpdateTopicRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.InvalidPartitionsException;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Servicio administrativo para gestionar topicos y configuraciones en el cluster de Apache Kafka
 * utilizando el cliente oficial AdminClient.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaAdminService {

    private static final long ADMIN_TIMEOUT_SECONDS = 15L;

    private final KafkaAdmin kafkaAdmin;

    private AdminClient createAdminClient() {
        return AdminClient.create(kafkaAdmin.getConfigurationProperties());
    }

    /**
     * Crea un nuevo topico en Apache Kafka con particiones, factor de replicacion y configuraciones opcionales.
     */
    public AdminOperationResponseDTO createTopic(CreateTopicRequestDTO request) {
        log.info("[Kafka Admin] Creando topico '{}' (particiones={}, replicas={}, configs={})",
                request.getName(), request.getPartitions(), request.getReplicationFactor(), request.getConfigs());

        NewTopic newTopic = new NewTopic(request.getName(), request.getPartitions(), request.getReplicationFactor());
        if (request.getConfigs() != null && !request.getConfigs().isEmpty()) {
            newTopic.configs(request.getConfigs());
        }

        try (AdminClient client = createAdminClient()) {
            client.createTopics(Collections.singletonList(newTopic))
                    .all()
                    .get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            log.info("[Kafka Admin] Topico '{}' creado exitosamente", request.getName());
            return AdminOperationResponseDTO.builder()
                    .status("SUCCESS")
                    .message("Topico creado exitosamente en el cluster Kafka")
                    .resourceType("KAFKA_TOPIC")
                    .resourceName(request.getName())
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (ExecutionException e) {
            throw mapKafkaExecutionException(e, request.getName(), "crear");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Operacion interrumpida al crear topico");
        } catch (TimeoutException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Tiempo de espera agotado al conectar con el cluster Kafka");
        }
    }

    /**
     * Lista todos los topicos disponibles en el cluster Kafka.
     */
    public List<String> listTopics(boolean includeInternal) {
        try (AdminClient client = createAdminClient()) {
            ListTopicsOptions options = new ListTopicsOptions().listInternal(includeInternal);
            Set<String> names = client.listTopics(options)
                    .names()
                    .get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            return names.stream().sorted().collect(Collectors.toList());
        } catch (ExecutionException e) {
            throw mapKafkaExecutionException(e, "cluster", "listar");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Operacion interrumpida al listar topicos");
        } catch (TimeoutException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Tiempo de espera agotado al conectar con el cluster Kafka");
        }
    }

    /**
     * Obtiene el detalle completo de un topico (particiones, lideres, replicas, ISR y configuraciones).
     */
    public TopicInfoDTO getTopicInfo(String topicName) {
        try (AdminClient client = createAdminClient()) {
            Map<String, TopicDescription> descriptions = client
                    .describeTopics(Collections.singletonList(topicName))
                    .allTopicNames()
                    .get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            TopicDescription description = descriptions.get(topicName);
            if (description == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Topico no encontrado: " + topicName);
            }

            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
            Map<ConfigResource, Config> configsMap = client
                    .describeConfigs(Collections.singletonList(resource))
                    .all()
                    .get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Map<String, String> topicConfigs = new TreeMap<>();
            Config config = configsMap.get(resource);
            if (config != null) {
                for (ConfigEntry entry : config.entries()) {
                    if (!entry.isSensitive() && entry.value() != null) {
                        topicConfigs.put(entry.name(), entry.value());
                    }
                }
            }

            List<TopicInfoDTO.PartitionDetailDTO> partitionDetails = description.partitions().stream()
                    .map(p -> TopicInfoDTO.PartitionDetailDTO.builder()
                            .partition(p.partition())
                            .leaderId(p.leader() != null ? p.leader().id() : -1)
                            .replicas(p.replicas().stream().map(Node::id).collect(Collectors.toList()))
                            .isr(p.isr().stream().map(Node::id).collect(Collectors.toList()))
                            .build())
                    .collect(Collectors.toList());

            int replicationFactor = description.partitions().isEmpty()
                    ? 0
                    : description.partitions().get(0).replicas().size();

            return TopicInfoDTO.builder()
                    .name(description.name())
                    .internal(description.isInternal())
                    .partitionCount(description.partitions().size())
                    .replicationFactor(replicationFactor)
                    .partitions(partitionDetails)
                    .configs(topicConfigs)
                    .build();
        } catch (ExecutionException e) {
            throw mapKafkaExecutionException(e, topicName, "consultar");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Operacion interrumpida al consultar topico");
        } catch (TimeoutException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Tiempo de espera agotado al conectar con el cluster Kafka");
        }
    }

    /**
     * Modifica un topico existente: permite incrementar el numero de particiones y/o
     * actualizar configuraciones dinamicas (ej: retention.ms, retention.bytes, cleanup.policy).
     */
    public AdminOperationResponseDTO updateTopic(String topicName, UpdateTopicRequestDTO request) {
        boolean hasPartitions = request.getNewPartitions() != null;
        boolean hasConfigs = request.getConfigs() != null && !request.getConfigs().isEmpty();

        if (!hasPartitions && !hasConfigs) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe especificar al menos 'newPartitions' o 'configs' para modificar el topico");
        }

        log.info("[Kafka Admin] Modificando topico '{}' (newPartitions={}, configs={})",
                topicName, request.getNewPartitions(), request.getConfigs());

        try (AdminClient client = createAdminClient()) {
            // Verificar que el topico existe
            Map<String, TopicDescription> descriptions = client
                    .describeTopics(Collections.singletonList(topicName))
                    .allTopicNames()
                    .get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            TopicDescription current = descriptions.get(topicName);
            List<String> changesApplied = new ArrayList<>();

            if (hasPartitions) {
                int currentPartitions = current.partitions().size();
                if (request.getNewPartitions() <= currentPartitions) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            String.format("El nuevo numero de particiones (%d) debe ser mayor al actual (%d)",
                                    request.getNewPartitions(), currentPartitions));
                }

                Map<String, NewPartitions> partitionMap = Collections.singletonMap(
                        topicName, NewPartitions.increaseTo(request.getNewPartitions()));
                client.createPartitions(partitionMap)
                        .all()
                        .get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                changesApplied.add("particiones incrementadas a " + request.getNewPartitions());
            }

            if (hasConfigs) {
                ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
                Collection<AlterConfigOp> alterOps = request.getConfigs().entrySet().stream()
                        .map(entry -> new AlterConfigOp(
                                new ConfigEntry(entry.getKey(), entry.getValue()),
                                AlterConfigOp.OpType.SET))
                        .collect(Collectors.toList());

                client.incrementalAlterConfigs(Collections.singletonMap(resource, alterOps))
                        .all()
                        .get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                changesApplied.add("configuraciones actualizadas: " + request.getConfigs().keySet());
            }

            String summary = "Topico modificado exitosamente (" + String.join("; ", changesApplied) + ")";
            log.info("[Kafka Admin] {}", summary);

            return AdminOperationResponseDTO.builder()
                    .status("SUCCESS")
                    .message(summary)
                    .resourceType("KAFKA_TOPIC")
                    .resourceName(topicName)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (ExecutionException e) {
            throw mapKafkaExecutionException(e, topicName, "modificar");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Operacion interrumpida al modificar topico");
        } catch (TimeoutException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Tiempo de espera agotado al conectar con el cluster Kafka");
        }
    }

    /**
     * Elimina un topico del cluster de Apache Kafka.
     */
    public AdminOperationResponseDTO deleteTopic(String topicName) {
        log.info("[Kafka Admin] Eliminando topico '{}'", topicName);

        try (AdminClient client = createAdminClient()) {
            client.deleteTopics(Collections.singletonList(topicName))
                    .all()
                    .get(ADMIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            log.info("[Kafka Admin] Topico '{}' eliminado exitosamente", topicName);
            return AdminOperationResponseDTO.builder()
                    .status("SUCCESS")
                    .message("Topico eliminado exitosamente del cluster Kafka")
                    .resourceType("KAFKA_TOPIC")
                    .resourceName(topicName)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (ExecutionException e) {
            throw mapKafkaExecutionException(e, topicName, "eliminar");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Operacion interrumpida al eliminar topico");
        } catch (TimeoutException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Tiempo de espera agotado al conectar con el cluster Kafka");
        }
    }

    private ResponseStatusException mapKafkaExecutionException(ExecutionException e, String topicName, String action) {
        Throwable cause = e.getCause();
        if (cause instanceof TopicExistsException) {
            return new ResponseStatusException(HttpStatus.CONFLICT,
                    "El topico '" + topicName + "' ya existe en el cluster Kafka");
        }
        if (cause instanceof UnknownTopicOrPartitionException) {
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "El topico '" + topicName + "' no existe en el cluster Kafka");
        }
        if (cause instanceof InvalidReplicationFactorException || cause instanceof InvalidPartitionsException) {
            return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Parametros invalidos al " + action + " el topico '" + topicName + "': " + cause.getMessage());
        }
        log.error("[Kafka Admin] Error al {} el topico '{}': {}", action, topicName,
                cause != null ? cause.getMessage() : e.getMessage());
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al " + action + " el topico '" + topicName + "': " + (cause != null ? cause.getMessage() : e.getMessage()));
    }
}
