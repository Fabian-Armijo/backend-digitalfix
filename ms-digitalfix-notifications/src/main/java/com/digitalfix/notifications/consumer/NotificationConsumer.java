package com.digitalfix.notifications.consumer;

import com.digitalfix.notifications.config.RabbitMQConfig;
import com.digitalfix.notifications.model.WorkOrderEvent;
import com.digitalfix.notifications.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeWorkOrderEvent(WorkOrderEvent event) {
        log.info("📥 [RabbitMQ Consumer] Evento recibido desde la cola '{}': Orden #{} | Estado='{}' | EventType='{}'",
                RabbitMQConfig.QUEUE_NAME, event.getOrderId(), event.getStatus(), event.getEventType());

        try {
            // Procesamiento asíncrono y despacho del correo al cliente
            emailService.sendWorkOrderNotification(event);
            log.info("✅ [RabbitMQ Consumer] Notificación procesada exitosamente para orden #{}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ [RabbitMQ Consumer] Error al procesar notificación de orden #{}: {}",
                    event.getOrderId(), e.getMessage());
            // Relanzamos para que Spring AMQP aplique la política de reintentos o mueva el mensaje a la DLQ
            throw e;
        }
    }
}
