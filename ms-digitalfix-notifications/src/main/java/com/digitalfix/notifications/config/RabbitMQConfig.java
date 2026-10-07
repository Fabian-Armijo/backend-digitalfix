package com.digitalfix.notifications.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // Exchange principal del dominio de órdenes de trabajo
    public static final String EXCHANGE_NAME = "workorders.exchange";

    // Cola principal de notificaciones
    public static final String QUEUE_NAME = "workorders.notifications.queue";

    // Dead Letter Exchange (DLX) y Dead Letter Queue (DLQ) para tolerancia a fallos
    public static final String DLX_NAME = "workorders.dlx";
    public static final String DLQ_NAME = "workorders.notifications.dlq";
    public static final String DLQ_ROUTING_KEY = "workorders.notifications.dlq.key";

    // Patrón de escucha: Captura todos los eventos que comiencen con "workorder."
    public static final String ROUTING_KEY_PATTERN = "workorder.#";

    @Bean
    public TopicExchange workOrdersExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DLQ_ROUTING_KEY);
    }

    @Bean
    public Queue notificationsQueue() {
        // Configuramos argumentos para la cola de notificaciones
        Map<String, Object> args = new HashMap<>();
        // Redirección automática a la DLQ en caso de mensaje rechazado
        args.put("x-dead-letter-exchange", DLX_NAME);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        // Soporte de replicación en cluster (Quorum Queue para alta disponibilidad)
        args.put("x-queue-type", "quorum");

        return new Queue(QUEUE_NAME, true, false, false, args);
    }

    @Bean
    public Binding notificationsBinding() {
        return BindingBuilder.bind(notificationsQueue())
                .to(workOrdersExchange())
                .with(ROUTING_KEY_PATTERN);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        // Reintentos y concurrencia
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
