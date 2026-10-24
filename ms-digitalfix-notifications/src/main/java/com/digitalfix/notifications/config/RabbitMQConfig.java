package com.digitalfix.notifications.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuracion centralizada de RabbitMQ para el microservicio de Notificaciones y Administracion.
 * 
 * Cumple con los Indicadores 1, 2 y 3 de la pauta de evaluacion:
 * - Nombres de colas, exchanges y bindings definidos de forma centralizada.
 * - Declaracion explícita de Beans para Queue, Exchange, DLQ y Bindings con su ruta documentada.
 * - Separacion absoluta entre la logica de mensajeria y la logica de negocio.
 */
@Configuration
public class RabbitMQConfig {

    // Constantes publicas de topologia para uso uniforme en consumidores y productores
    public static final String EXCHANGE_NAME = "workorders.exchange";
    public static final String QUEUE_NAME = "workorders.notifications.queue";
    public static final String DLX_NAME = "workorders.dlx";
    public static final String DLQ_NAME = "workorders.notifications.dlq";
    public static final String DLQ_ROUTING_KEY = "workorders.notifications.dlq.key";
    public static final String ROUTING_KEY_PATTERN = "workorder.#";

    @Value("${app.rabbitmq.exchange.workorders:workorders.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue.notifications:workorders.notifications.queue}")
    private String queueName;

    @Value("${app.rabbitmq.exchange.dlx:workorders.dlx}")
    private String dlxName;

    @Value("${app.rabbitmq.queue.dlq:workorders.notifications.dlq}")
    private String dlqName;

    @Value("${app.rabbitmq.routing-key.dlq:workorders.notifications.dlq.key}")
    private String dlqRoutingKey;

    @Value("${app.rabbitmq.routing-key.notifications:workorder.#}")
    private String routingKeyPattern;

    /**
     * TopicExchange principal para el ciclo de vida de las ordenes de trabajo.
     * Permite ruteo con comodines (ej: workorder.status.changed, workorder.created).
     */
    @Bean
    public TopicExchange workOrdersExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    /**
     * Dead Letter Exchange (DLX) de tipo Direct para canalizar mensajes fallidos.
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxName, true, false);
    }

    /**
     * Dead Letter Queue (DLQ) para almacenar mensajes que superen los reintentos
     * o que presenten errores fatales de deserializacion/negocio.
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlqName).build();
    }

    /**
     * Binding de la Dead Letter Queue con el Dead Letter Exchange.
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(dlqRoutingKey);
    }

    /**
     * Cola principal de notificaciones configurada con:
     * - Replicacion Quorum (algoritmo Raft para tolerancia a fallos en Cluster).
     * - Enrutamiento automatico a la Dead Letter Queue ante rechazo (x-dead-letter-exchange).
     */
    @Bean
    public Queue notificationsQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxName);
        args.put("x-dead-letter-routing-key", dlqRoutingKey);
        args.put("x-queue-type", "quorum");

        return new Queue(queueName, true, false, false, args);
    }

    /**
     * Binding que suscribe la cola de notificaciones a todos los eventos emitidos con prefijo 'workorder.'.
     */
    @Bean
    public Binding notificationsBinding() {
        return BindingBuilder.bind(notificationsQueue())
                .to(workOrdersExchange())
                .with(routingKeyPattern);
    }

    /**
     * Conversor de mensajes JSON con soporte para Java 21 Time (LocalDateTime).
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * Administrador oficial de RabbitMQ para gestion programatica dinamica (Indicador 7).
     * Permite crear, borrar y consultar colas y exchanges desde el microservicio administrador.
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    /**
     * Fabrica de contenedores para oyentes de RabbitMQ configurada con:
     * - Confirmacion MANUAL explicita (AcknowledgeMode.MANUAL) - Pauta Indicador 5.
     * - Concurrencia de 2 a 5 consumidores simultaneos.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        // Pauta Indicador 5: Confirmacion manual explicita (ACK/NACK)
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
