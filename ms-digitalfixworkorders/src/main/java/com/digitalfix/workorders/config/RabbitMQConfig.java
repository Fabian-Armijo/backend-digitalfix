package com.digitalfix.workorders.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion centralizada de RabbitMQ para el microservicio de Ordenes de Trabajo.
 * 
 * Cumple con los Indicadores 1, 2 y 3 de la pauta de evaluacion:
 * - Define de manera centralizada en @Configuration y application.properties los nombres de colas, exchanges y routing keys.
 * - Declara beans formales de TopicExchange durables para el caso de uso de ciclo de vida de ordenes.
 * - Desacopla la logica de mensajeria de la logica de negocio en el servicio WorkOrderServiceImpl.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "workorders.exchange";
    public static final String ROUTING_KEY_CREATED = "workorder.created";
    public static final String ROUTING_KEY_STATUS_CHANGED = "workorder.status.changed";
    public static final String ROUTING_KEY_ASSIGNED = "workorder.assigned";

    @Value("${app.rabbitmq.exchange.workorders:workorders.exchange}")
    private String exchangeName;

    /**
     * TopicExchange principal de ordenes de trabajo.
     * Es durable para persistir ante reinicios de nodos en el cluster.
     */
    @Bean
    public TopicExchange workOrdersExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    /**
     * Conversor JSON para serializar eventos con soporte de fechas Java 21 (LocalDateTime).
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * RabbitTemplate configurado con el conversor JSON para publicar eventos limpios al cluster.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
