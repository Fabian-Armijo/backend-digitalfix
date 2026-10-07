package com.digitalfix.notifications.service;

import com.digitalfix.notifications.model.WorkOrderEvent;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:notificaciones@digitalfix.com}")
    private String fromEmail;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public void sendWorkOrderNotification(WorkOrderEvent event) {
        String recipient = resolveCustomerEmail(event.getCustomerId());
        String subject = String.format("[DigitalFix] Orden #%d: %s", event.getOrderId(), getStatusTitle(event.getStatus()));
        String htmlBody = buildHtmlEmail(event, recipient);

        // 1. Mostrar banner estructurado en los logs (para monitoreo y demostración en vivo)
        logEmailBanner(event, recipient, subject);

        // 2. Si hay un servidor SMTP configurado con credenciales, despachar el correo real
        if (mailSender != null && mailUsername != null && !mailUsername.isBlank()) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromEmail);
                helper.setTo(recipient);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);

                mailSender.send(message);
                log.info("✅ [SMTP] Correo enviado exitosamente a: {}", recipient);
            } catch (Exception e) {
                log.warn("⚠️ [SMTP] No se pudo enviar el correo físico ({}), pero la notificación fue procesada correctamente.", e.getMessage());
            }
        } else {
            log.info("ℹ️ [SIMULADOR SMTP] Servidor SMTP en modo local/pruebas. Notificación registrada en consola.");
        }
    }

    private String resolveCustomerEmail(Long customerId) {
        if (customerId == null) return "cliente@digitalfix.com";
        return String.format("cliente%d@digitalfix.com", customerId);
    }

    private String getStatusTitle(String status) {
        if (status == null) return "Actualización de Orden";
        return switch (status.toUpperCase()) {
            case "CREADA" -> "Solicitud Registrada 📝";
            case "ASIGNADA" -> "Técnico Asignado 👨‍🔧";
            case "EN_DESPLAZAMIENTO" -> "Técnico en Camino a tu Domicilio 🛵";
            case "EN_EJECUCION" -> "Mantención en Curso ⚙️";
            case "CERRADA" -> "Mantención Finalizada con Éxito ✅";
            case "CANCELADA" -> "Orden Cancelada ❌";
            default -> "Estado Actualizado: " + status;
        };
    }

    private String getStatusDescription(String status) {
        if (status == null) return "Tu orden ha cambiado de estado.";
        return switch (status.toUpperCase()) {
            case "CREADA" -> "Hemos recibido tu solicitud de mantenimiento y está a la espera de un especialista.";
            case "ASIGNADA" -> "Un técnico especialista ha sido asignado a tu requerimiento.";
            case "EN_DESPLAZAMIENTO" -> "¡El técnico ha iniciado el traslado y se dirige a tu ubicación!";
            case "EN_EJECUCION" -> "El técnico se encuentra realizando el trabajo técnico en tu equipo.";
            case "CERRADA" -> "¡El trabajo técnico ha concluido satisfactoriamente! Tu equipo está listo.";
            case "CANCELADA" -> "La orden ha sido cancelada por el área de supervisión.";
            default -> "Se ha registrado un avance en tu orden de trabajo.";
        };
    }

    private String buildHtmlEmail(WorkOrderEvent event, String recipient) {
        String fecha = event.getTimestamp() != null ? event.getTimestamp().format(FORMATTER) : "Reciente";
        String tecnico = event.getTechnicianId() != null ? "#" + event.getTechnicianId() : "Pendiente de asignación";

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f1f5f9; margin: 0; padding: 20px; color: #334155; }
                .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
                .header { background: linear-gradient(135deg, #1e293b 0%%, #0f172a 100%%); padding: 30px; text-align: center; color: #ffffff; }
                .header h1 { margin: 0; font-size: 24px; color: #38bdf8; letter-spacing: 1px; }
                .header p { margin: 5px 0 0 0; color: #94a3b8; font-size: 14px; }
                .content { padding: 30px; }
                .badge { display: inline-block; padding: 8px 16px; border-radius: 20px; font-weight: bold; font-size: 14px; background: #e0f2fe; color: #0369a1; margin-bottom: 20px; }
                .details-table { width: 100%%; border-collapse: collapse; margin-top: 20px; background: #f8fafc; border-radius: 8px; overflow: hidden; }
                .details-table td { padding: 12px 16px; border-bottom: 1px solid #e2e8f0; font-size: 14px; }
                .details-table td.label { font-weight: 600; color: #64748b; width: 40%%; }
                .footer { background: #f8fafc; padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>DIGITALFIX</h1>
                  <p>Sistema Integral de Órdenes de Trabajo</p>
                </div>
                <div class="content">
                  <div class="badge">%s</div>
                  <h2 style="margin-top:0; color:#1e293b;">Hola, %s</h2>
                  <p style="font-size: 15px; line-height: 1.6;">%s</p>
                  
                  <table class="details-table">
                    <tr><td class="label">N° de Orden</td><td><strong>#%d</strong></td></tr>
                    <tr><td class="label">Estado Actual</td><td><strong>%s</strong></td></tr>
                    <tr><td class="label">Técnico Asignado</td><td>%s</td></tr>
                    <tr><td class="label">Descripción</td><td>%s</td></tr>
                    <tr><td class="label">Fecha / Hora</td><td>%s</td></tr>
                  </table>
                </div>
                <div class="footer">
                  <p>© 2026 DigitalFix. Notificación automática del sistema asíncrono.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                getStatusTitle(event.getStatus()),
                recipient,
                getStatusDescription(event.getStatus()),
                event.getOrderId() != null ? event.getOrderId() : 0,
                event.getStatus() != null ? event.getStatus() : "N/A",
                tecnico,
                event.getDescription() != null ? event.getDescription() : "Sin descripción",
                fecha
            );
    }

    private void logEmailBanner(WorkOrderEvent event, String recipient, String subject) {
        String fecha = event.getTimestamp() != null ? event.getTimestamp().format(FORMATTER) : "N/A";
        log.info("""
            
            ╔══════════════════════════════════════════════════════════════════════════════════╗
            ║                    📧 NOTIFICACIÓN POR CORREO DESPACHADA                         ║
            ╠══════════════════════════════════════════════════════════════════════════════════╣
            ║ Destinatario : {}
            ║ Asunto       : {}
            ║ N° Orden     : #{}
            ║ Estado       : {}
            ║ Técnico      : {}
            ║ Fecha/Hora   : {}
            ║ Tipo Evento  : {}
            ╚══════════════════════════════════════════════════════════════════════════════════╝
            """,
            recipient,
            subject,
            event.getOrderId(),
            event.getStatus(),
            event.getTechnicianId() != null ? "#" + event.getTechnicianId() : "Sin asignar",
            fecha,
            event.getEventType()
        );
    }
}
