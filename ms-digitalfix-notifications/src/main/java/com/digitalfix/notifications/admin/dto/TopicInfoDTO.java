package com.digitalfix.notifications.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO con informacion detallada de un topico de Apache Kafka (particiones, replicas y configuraciones).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicInfoDTO {
    private String name;
    private boolean internal;
    private int partitionCount;
    private int replicationFactor;
    private List<PartitionDetailDTO> partitions;
    private Map<String, String> configs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartitionDetailDTO {
        private int partition;
        private int leaderId;
        private List<Integer> replicas;
        private List<Integer> isr;
    }
}
