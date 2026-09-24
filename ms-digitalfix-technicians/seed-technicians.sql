-- Script para insertar 5 técnicos iniciales en la base de datos DigitalFix
-- Tabla: technicians

INSERT INTO technicians (first_name, last_name, email, phone, specialty, status, created_at, updated_at)
VALUES
  ('Carlos', 'Mendoza', 'carlos.mendoza@digitalfix.com', '+56911223344', 'Reparación de Placas y Microelectrónica', 'DISPONIBLE', NOW(), NOW()),
  ('Valeria', 'Rojas', 'valeria.rojas@digitalfix.com', '+56922334455', 'Cambio de Pantallas y Baterías', 'DISPONIBLE', NOW(), NOW()),
  ('Andrés', 'Silva', 'andres.silva@digitalfix.com', '+56933445566', 'Diagnóstico y Hardware de Laptops', 'OCUPADO', NOW(), NOW()),
  ('Camila', 'Fuentes', 'camila.fuentes@digitalfix.com', '+56944556677', 'Software, Flasheo y Recuperación de Datos', 'DISPONIBLE', NOW(), NOW()),
  ('Matías', 'Herrera', 'matias.herrera@digitalfix.com', '+56955667788', 'Mantenimiento Preventivo y Limpieza', 'INACTIVO', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
