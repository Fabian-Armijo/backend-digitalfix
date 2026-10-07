-- ====================================================================
-- Script de inicialización de datos: ms-digitalfixworkorders
-- Base de datos: digitalfix_workorders_db
-- Descripción: Inserta órdenes de trabajo en distintas fases del ciclo
-- ====================================================================

TRUNCATE TABLE work_orders RESTART IDENTITY CASCADE;

INSERT INTO work_orders (customer_id, technician_id, description, status, created_at, updated_at)
VALUES
  (1, NULL, 'Mantención de equipo: Mantenimiento Preventivo y Limpieza Térmica | Notas del cliente: Laptop ASUS Rog Strix sobrecalienta rápido y emite ruido fuerte en los ventiladores.', 'CREADA', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
  (1, 1, 'Mantención de equipo: Diagnóstico General de Hardware | Notas del cliente: Computador de escritorio no emite video y emite 3 beeps continuos al encender.', 'ASIGNADA', NOW() - INTERVAL '1 day', NOW() - INTERVAL '18 hours'),
  (2, 2, 'Mantención de equipo: Cambio de Pantalla o Módulo Display | Notas del cliente: Laptop sufrió caída en la oficina, display parpadea y tiene líneas verticales.', 'EN_DESPLAZAMIENTO', NOW() - INTERVAL '1 day 4 hours', NOW() - INTERVAL '3 hours'),
  (1, 3, 'Mantención de equipo: Reparación de Placa Madre y Microelectrónica | Notas del cliente: Notebook Dell no enciende tras tormenta eléctrica. Posible daño en circuito de carga.', 'EN_EJECUCION', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day'),
  (2, 4, 'Mantención de equipo: Reinstalación de Sistema Operativo y Respaldo | Notas del cliente: Windows no inicia después de actualización fallida. Se realizó respaldo y reinstalación exitosa.', 'CERRADA', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days'),
  (3, NULL, 'Mantención de equipo: Recuperación Avanzada de Datos | Notas del cliente: Solicitud ingresada por error, el cliente recuperó sus archivos desde la copia de Google Drive.', 'CANCELADA', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days');
