package com.digitalfix.workorders.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuracion optimizada del Productor de Apache Kafka para ms-digitalfixworkorders.
 *
 * Incluye tunning de alto rendimiento:
 * - batch.size: 32 KB para agrupar mensajes en lotes eficientes.
 * - linger.ms: 10 ms de espera para maximizar el llenado de lotes.
 * - compression.type: snappy para reducir trafico de red con minimo consumo de CPU.
 * - acks: all + idempotencia para garantizar durabilidad y cero duplicados en auditoria.
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092,localhost:9093}")
    private String bootstrapServers;

    @Value("${app.kafka.topic.audit:workorders.audit.events}")
    private String auditTopicName;

    @Value("${app.kafka.topic.stress:workorders.stress.test}")
    private String stressTopicName;

    @Value("${app.kafka.producer.batch-size:32768}")
    private int batchSize;

    @Value("${app.kafka.producer.linger-ms:10}")
    private int lingerMs;

    @Value("${app.kafka.producer.compression-type:snappy}")
    private String compressionType;

    @Value("${app.kafka.producer.acks:all}")
    private String acks;

    @Value("${app.kafka.topic.partitions:3}")
    private int defaultPartitions;

    @Value("${app.kafka.topic.replicas:2}")
    private int defaultReplicas;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        KafkaAdmin admin = new KafkaAdmin(configs);
        // No detener el arranque del microservicio si el cluster Kafka aun se esta iniciando
        admin.setFatalIfBrokerNotAvailable(false);
        return admin;
    }

    /**
     * Topico principal de auditoria inmutable de ordenes de trabajo.
     * Politica de retencion: delete, 7 dias (604800000 ms), limite 1 GB por particion.
     */
    @Bean
    public NewTopic workOrdersAuditTopic() {
        return TopicBuilder.name(auditTopicName)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                .config(TopicConfig.RETENTION_BYTES_CONFIG, "1073741824")
                .build();
    }

    /**
     * Topico dedicado para pruebas de estres y medicion de rendimiento/latencia.
     * Politica de retencion corta (24 horas) para no ocupar disco innecesariamente.
     */
    @Bean
    public NewTopic workOrdersStressTopic() {
        return TopicBuilder.name(stressTopicName)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
                .config(TopicConfig.RETENTION_BYTES_CONFIG, "536870912")
                .build();
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Configuracion de rendimiento optimizado
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        configProps.put(ProducerConfig.ACKS_CONFIG, acks);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
