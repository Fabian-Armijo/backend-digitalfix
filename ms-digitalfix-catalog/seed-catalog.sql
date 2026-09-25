-- ====================================================================
-- Script de inicialización de datos: ms-digitalfix-catalog
-- Base de datos: digitalfix_catalog_db
-- Descripción: Inserta servicios técnicos y repuestos con stock variado
-- ====================================================================

TRUNCATE TABLE technical_services RESTART IDENTITY CASCADE;
TRUNCATE TABLE spare_parts RESTART IDENTITY CASCADE;

-- 1. Servicios Técnicos
INSERT INTO technical_services (name, fee, description)
VALUES
  ('Mantenimiento Preventivo y Limpieza Térmica', 25000.00, 'Limpieza interna profunda de ventiladores y disipadores, con cambio de pasta térmica de alto rendimiento.'),
  ('Diagnóstico General de Hardware', 15000.00, 'Revisión técnica de componentes físicos, pruebas de estabilidad bajo carga de CPU, memorias y tarjeta gráfica.'),
  ('Reinstalación de Sistema Operativo y Respaldo', 20000.00, 'Instalación limpia de sistema operativo, optimización del sistema, instalación de drivers oficiales y respaldo de archivos.'),
  ('Reparación de Placa Madre y Microelectrónica', 65000.00, 'Detección de cortos a nivel de componentes SMD, reemplazo de integrados, MOSFETs y circuitos de alimentación.'),
  ('Cambio de Pantalla o Módulo Display', 30000.00, 'Reemplazo e instalación técnica de paneles LED/OLED dañados para laptops o dispositivos móviles.'),
  ('Recuperación Avanzada de Datos', 45000.00, 'Extracción y rescate de archivos en unidades mecánicas HDD, memorias flash o SSD con sectores defectuosos.');

-- 2. Repuestos (Incluye ítems con stock crítico <= 3 para reflejar alertas en el Dashboard)
INSERT INTO spare_parts (name, cost, stock)
VALUES
  ('Pasta Térmica Arctic MX-4 (4g)', 8500.00, 20),
  ('SSD Kingston NV2 1TB M.2 NVMe PCIe 4.0', 58000.00, 14),
  ('Memoria RAM Crucial 16GB DDR4 3200MHz', 38000.00, 8),
  ('Pantalla LED 15.6" Slim Full HD (30 Pines)', 62000.00, 2),
  ('Batería Laptop HP Pavilion / Envy Original', 35000.00, 3),
  ('Cargador Universal Notebook 65W Tipo-C USB-PD', 22000.00, 12),
  ('Ventilador Cooler CPU Asus TUF Gaming', 18500.00, 6);
