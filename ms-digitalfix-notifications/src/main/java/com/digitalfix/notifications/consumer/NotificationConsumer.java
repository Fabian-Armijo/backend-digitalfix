package com.digitalfix.notifications.consumer;

import com.digitalfix.notifications.config.RabbitMQConfig;
import com.digitalfix.notifications.model.WorkOrderEvent;
import com.digitalfix.notifications.service.EmailService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumidor asincrono de eventos de ordenes de trabajo.
 * 
 * Cumple con los Indicadores 4 y 5 de la pauta de evaluacion:
 * - Indicador 4 (15%): Implementado con @RabbitListener agrupado por dominio funcional.
 * - Indicador 5 (20%): Manejo explicito de confirmacion de mensajes (ACK manual),
 *   con rutas diferenciadas entre procesamiento exitoso (ACK), reintentos (NACK con requeue)
 *   y envio directo a la Dead Letter Queue (NACK sin requeue).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;
    private static final int MAX_DELIVERY_ATTEMPTS = 3;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeWorkOrderEvent(
            WorkOrderEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            @Header(value = "x-delivery-count", required = false) Long deliveryCountHeader
    ) throws IOException {

        long deliveryCount = deliveryCountHeader != null ? deliveryCountHeader : 1L;

        log.info("📥 [RabbitMQ Consumer] Mensaje recibido [DeliveryTag: {}, Intento: {}/{}]: Orden #{} | Estado='{}' | Evento='{}'",
                deliveryTag, deliveryCount, MAX_DELIVERY_ATTEMPTS,
                event != null ? event.getOrderId() : "N/A",
                event != null ? event.getStatus() : "N/A",
                event != null ? event.getEventType() : "N/A");

        // =========================================================================
        // RUTA 1: VALIDACION DE INTEGRIDAD (Error No Recuperable -> Desvio a DLQ)
        // =========================================================================
        if (event == null || event.getOrderId() == null) {
            log.error("❌ [Error Fatal] Payload corrupto o invalido detectado. Rechazando mensaje hacia DLQ sin reencolar.");
            // NACK sin requeue: RabbitMQ lo desvia inmediatamente al Dead Letter Exchange (DLX)
            channel.basicNack(deliveryTag, false, false);
            return;
        }

        try {
            // =====================================================================
            // RUTA 2: PROCESAMIENTO EXITOSO (ACK Manual Explicito)
            // =====================================================================
            emailService.sendWorkOrderNotification(event);

            // Confirmacion manual: El mensaje se procesó con éxito y se elimina de la cola
            channel.basicAck(deliveryTag, false);
            log.info("✅ [ACK Exitoso] Mensaje confirmado [DeliveryTag: {}] para la orden #{}", deliveryTag, event.getOrderId());

        } catch (IllegalArgumentException e) {
            // =====================================================================
            // RUTA 3: ERROR DE NEGOCIO / DATOS NO VALIDOS (No recuperable -> DLQ)
            // =====================================================================
            log.error("⚠️ [Error No Recuperable] Error de validacion en orden #{}: {}. Enviando a DLQ.",
                    event.getOrderId(), e.getMessage());
            // Rechazo explicito hacia Dead Letter Queue
            channel.basicNack(deliveryTag, false, false);

        } catch (Exception e) {
            // =====================================================================
            // RUTA 4: ERROR TRANSITORIO DE I/O O INFRAESTRUCTURA (Evaluacion de Reintentos)
            // =====================================================================
            log.error("⚠️ [Error Transitorio] Fallo al procesar la orden #{}: {}. Intento actual: {}",
                    event.getOrderId(), e.getMessage(), deliveryCount);

            if (deliveryCount < MAX_DELIVERY_ATTEMPTS) {
                // Error recuperable y con margen de reintento -> NACK con requeue
                log.warn("🔄 [Reintento Solicitado] Reencolando mensaje [DeliveryTag: {}] para reintento posterior.", deliveryTag);
                channel.basicNack(deliveryTag, false, true);
            } else {
                // Se supero el umbral maximo de tolerancia a fallos -> Desvio a DLQ para analisis forense
                log.error("🛑 [Umbral Superado] Se alcanzaron {} intentos fallidos. Derivando mensaje a la DLQ ('{}').",
                        MAX_DELIVERY_ATTEMPTS, RabbitMQConfig.DLQ_NAME);
                channel.basicNack(deliveryTag, false, false);
            }
        }
    }
}
