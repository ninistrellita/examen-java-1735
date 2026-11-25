USE carmotors;

-- Catálogos y datos de prueba
INSERT IGNORE INTO repuesto_tipos (nombre) VALUES ('Mecánico'), ('Eléctrico');
INSERT IGNORE INTO servicio_estados (nombre)
VALUES ('Pendiente'), ('En Proceso'), ('Completado'), ('Facturado'), ('Entregado');
INSERT IGNORE INTO mantenimiento_tipos (nombre) VALUES ('Preventivo'), ('Correctivo');

INSERT INTO clientes (nombre, documento_identidad, telefono, email)
SELECT 'Cliente de prueba', 'DEMO01', NULL, 'cliente@example.com' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM clientes
                  WHERE documento_identidad = 'DEMO01' OR email = 'cliente@example.com');

INSERT INTO vehiculos (cliente_id, placa, marca, modelo)
SELECT c.id, 'ABC123', 'Marca de prueba', 'Modelo de prueba'
FROM clientes c
WHERE c.documento_identidad = 'DEMO01'
  AND NOT EXISTS (SELECT 1 FROM vehiculos WHERE placa = 'ABC123');

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id, nombre_servicio)
SELECT v.id, t.id, CURRENT_DATE - INTERVAL 8 MONTH, CURRENT_DATE - INTERVAL 7 MONTH,
       'Cambio de aceite de prueba', 45000, e.id, 'Cambio de aceite'
FROM vehiculos v
JOIN mantenimiento_tipos t ON t.nombre = 'Preventivo'
JOIN servicio_estados e ON e.nombre = 'Entregado'
WHERE v.placa = 'ABC123'
  AND NOT EXISTS (SELECT 1 FROM servicios s
                  WHERE s.vehiculo_id = v.id AND s.descripcion_problema = 'Cambio de aceite de prueba');

-- Clientes
INSERT INTO clientes (nombre, documento_identidad, telefono, email)
SELECT 'Carlos Gómez', '1090111222', '3001234567', 'carlos@correo.com' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM clientes
                  WHERE documento_identidad = '1090111222' OR email = 'carlos@correo.com');

INSERT INTO clientes (nombre, documento_identidad, telefono, email)
SELECT 'sebas rosales', '1092942229', '3001214567', 'sebasrosales@correo.com' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM clientes
                  WHERE documento_identidad = '1092942229' OR email = 'sebasrosales@correo.com');

-- Vehículos
INSERT INTO vehiculos (cliente_id, placa, marca, modelo)
SELECT c.id, 'jadshfkjdf', 'bugati', '1'
FROM clientes c
WHERE c.email = 'carlos@correo.com'
  AND NOT EXISTS (SELECT 1 FROM vehiculos WHERE placa = 'jadshfkjdf');

INSERT INTO vehiculos (cliente_id, placa, marca, modelo)
SELECT c.id, 'dfmslkfm', 'ferrari', '3'
FROM clientes c
WHERE c.email = 'sebasrosales@correo.com'
  AND NOT EXISTS (SELECT 1 FROM vehiculos WHERE placa = 'dfmslkfm');

-- Proveedores y repuestos
INSERT INTO proveedores (nombre, contacto)
SELECT 'Repuestos El Motor', NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM proveedores WHERE nombre = 'Repuestos El Motor');

INSERT INTO repuestos (nombre, repuesto_tipo_id, proveedor_id, precio_compra, precio_venta, cantidad_stock, stock_minimo)
SELECT 'Pastillas de freno', t.id, p.id, 60000, 95000, 10, 3
FROM repuesto_tipos t
JOIN proveedores p ON p.nombre = 'Repuestos El Motor'
WHERE t.nombre = 'Mecánico'
  AND NOT EXISTS (SELECT 1 FROM repuestos r WHERE r.nombre = 'Pastillas de freno' AND r.proveedor_id = p.id);

INSERT INTO repuestos (nombre, repuesto_tipo_id, proveedor_id, precio_compra, precio_venta, cantidad_stock, stock_minimo)
SELECT 'Batería 12V', t.id, p.id, 280000, 380000, 6, 2
FROM repuesto_tipos t
JOIN proveedores p ON p.nombre = 'Repuestos El Motor'
WHERE t.nombre = 'Eléctrico'
  AND NOT EXISTS (SELECT 1 FROM repuestos r WHERE r.nombre = 'Batería 12V' AND r.proveedor_id = p.id);

INSERT INTO clientes (nombre, documento_identidad, email)
SELECT 'Cliente de prueba Andres', 'TEST-ANDRES', 'andres.prueba@example.com'
WHERE NOT EXISTS (SELECT 1 FROM clientes WHERE documento_identidad = 'TEST-ANDRES');
INSERT INTO vehiculos (cliente_id, placa, marca, modelo)
SELECT id, 'TST001', 'Renault', 'Logan' FROM clientes
WHERE documento_identidad = 'TEST-ANDRES'
  AND NOT EXISTS (SELECT 1 FROM vehiculos WHERE placa = 'TST001');
INSERT INTO tecnicos (nombre)
SELECT 'Tecnico de prueba Andres'
WHERE NOT EXISTS (SELECT 1 FROM tecnicos WHERE nombre = 'Tecnico de prueba Andres');


-- Datos de prueba de los reportes gerenciales.
-- Las tablas y los datos previos se crean en schema.sql y en las secciones anteriores.
-- Se puede ejecutar varias veces: no duplica técnicos, órdenes, asignaciones ni repuestos usados.
-- Resultados esperados: docs/reportes-resultados-esperados.md

-- Técnicos
INSERT INTO tecnicos (nombre)
SELECT 'Luis Pérez' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tecnicos WHERE nombre = 'Luis Pérez');
INSERT INTO tecnicos (nombre)
SELECT 'Ana Torres' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tecnicos WHERE nombre = 'Ana Torres');
INSERT INTO tecnicos (nombre)
SELECT 'Mario Díaz' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM tecnicos WHERE nombre = 'Mario Díaz');

SET @luis  = (SELECT MIN(id) FROM tecnicos WHERE nombre = 'Luis Pérez');
SET @ana   = (SELECT MIN(id) FROM tecnicos WHERE nombre = 'Ana Torres');
SET @mario = (SELECT MIN(id) FROM tecnicos WHERE nombre = 'Mario Díaz');

SET @preventivo = (SELECT id FROM mantenimiento_tipos WHERE nombre = 'Preventivo');
SET @correctivo = (SELECT id FROM mantenimiento_tipos WHERE nombre = 'Correctivo');
SET @pendiente  = (SELECT id FROM servicio_estados WHERE nombre = 'Pendiente');
SET @proceso    = (SELECT id FROM servicio_estados WHERE nombre = 'En Proceso');
SET @completado = (SELECT id FROM servicio_estados WHERE nombre = 'Completado');
SET @facturado  = (SELECT id FROM servicio_estados WHERE nombre = 'Facturado');
SET @entregado  = (SELECT id FROM servicio_estados WHERE nombre = 'Entregado');

SET @v1 = (SELECT id FROM vehiculos WHERE placa = 'ABC123');
SET @v2 = (SELECT id FROM vehiculos WHERE placa = 'jadshfkjdf');
SET @v3 = (SELECT id FROM vehiculos WHERE placa = 'dfmslkfm');

SET @pastillas = (SELECT MIN(id) FROM repuestos WHERE nombre = 'Pastillas de freno');
SET @bateria   = (SELECT MIN(id) FROM repuestos WHERE nombre = 'Batería 12V');

-- Órdenes de servicio (la descripción "Reporte: ..." identifica cada orden de prueba)
INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id, nombre_servicio)
SELECT @v1, @preventivo, '2026-10-05 08:00', '2026-10-06 17:00', 'Reporte: cambio de aceite', 45000, @entregado, 'Cambio de aceite'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM servicios WHERE descripcion_problema = 'Reporte: cambio de aceite');

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id, nombre_servicio)
SELECT @v2, @correctivo, '2026-10-12 09:30', '2026-10-14 12:00', 'Reporte: cambio de pastillas', 80000, @facturado, 'Revisión de frenos'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM servicios WHERE descripcion_problema = 'Reporte: cambio de pastillas');

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id, nombre_servicio)
SELECT @v3, @correctivo, '2026-10-20 14:00', NULL, 'Reporte: batería descargada', 30000, @completado, 'Cambio de batería'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM servicios WHERE descripcion_problema = 'Reporte: batería descargada');

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id, nombre_servicio)
SELECT @v1, @preventivo, '2026-11-03 10:00', NULL, 'Reporte: revisión general', 60000, @proceso, 'Revisión de frenos'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM servicios WHERE descripcion_problema = 'Reporte: revisión general');

INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, fecha_entrega,
                       descripcion_problema, costo_mano_obra, estado_id, nombre_servicio)
SELECT @v2, @correctivo, '2026-11-10 16:45', NULL, 'Reporte: ruido en la suspensión', 50000, @pendiente, 'Alineación'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM servicios WHERE descripcion_problema = 'Reporte: ruido en la suspensión');

-- Bases cargadas antes de que existiera nombre_servicio: se les pone el nombre a las órdenes de prueba
UPDATE servicios SET nombre_servicio = CASE descripcion_problema
        WHEN 'Cambio de aceite de prueba'      THEN 'Cambio de aceite'
        WHEN 'Reporte: cambio de aceite'       THEN 'Cambio de aceite'
        WHEN 'Reporte: cambio de pastillas'    THEN 'Revisión de frenos'
        WHEN 'Reporte: batería descargada'     THEN 'Cambio de batería'
        WHEN 'Reporte: revisión general'       THEN 'Revisión de frenos'
        WHEN 'Reporte: ruido en la suspensión' THEN 'Alineación'
    END
WHERE nombre_servicio IS NULL
  AND descripcion_problema IN ('Cambio de aceite de prueba', 'Reporte: cambio de aceite',
      'Reporte: cambio de pastillas', 'Reporte: batería descargada', 'Reporte: revisión general',
      'Reporte: ruido en la suspensión');

SET @s1 = (SELECT MIN(id) FROM servicios WHERE descripcion_problema = 'Reporte: cambio de aceite');
SET @s2 = (SELECT MIN(id) FROM servicios WHERE descripcion_problema = 'Reporte: cambio de pastillas');
SET @s3 = (SELECT MIN(id) FROM servicios WHERE descripcion_problema = 'Reporte: batería descargada');
SET @s4 = (SELECT MIN(id) FROM servicios WHERE descripcion_problema = 'Reporte: revisión general');
SET @s5 = (SELECT MIN(id) FROM servicios WHERE descripcion_problema = 'Reporte: ruido en la suspensión');

-- Técnicos asignados (la llave primaria evita duplicados; IGNORE salta los que ya existen)
INSERT IGNORE INTO servicios_tecnicos (servicio_id, tecnico_id) VALUES
    (@s1, @luis), (@s2, @luis), (@s2, @ana), (@s3, @ana), (@s4, @luis), (@s5, @mario);

-- Repuestos usados (misma idea: llave primaria servicio_id + repuesto_id)
INSERT IGNORE INTO servicios_repuestos (servicio_id, repuesto_id, cantidad_usada, precio_unitario_venta) VALUES
    (@s2, @pastillas, 2, 95000),
    (@s3, @bateria,   1, 380000),
    (@s4, @pastillas, 1, 95000);


-- Pruebas integrales de todos los módulos.
-- Las cantidades_stock representan existencias actuales, después de los consumos de ejemplo.
-- Los lotes describen esas existencias: cargarlos aquí no vuelve a sumar stock.
START TRANSACTION;

-- Completa únicamente los nombres vacíos de las órdenes de ejemplo anteriores.
UPDATE servicios
SET nombre_servicio = CASE descripcion_problema
    WHEN 'Cambio de aceite de prueba' THEN 'Cambio de aceite'
    WHEN 'Reporte: cambio de aceite' THEN 'Cambio de aceite'
    WHEN 'Reporte: cambio de pastillas' THEN 'Revisión de frenos'
    WHEN 'Reporte: batería descargada' THEN 'Cambio de batería'
    WHEN 'Reporte: revisión general' THEN 'Revisión general'
    WHEN 'Reporte: ruido en la suspensión' THEN 'Revisión de suspensión'
END
WHERE (nombre_servicio IS NULL OR TRIM(nombre_servicio) = '')
  AND descripcion_problema IN (
    'Cambio de aceite de prueba', 'Reporte: cambio de aceite',
    'Reporte: cambio de pastillas', 'Reporte: batería descargada',
    'Reporte: revisión general', 'Reporte: ruido en la suspensión'
  );

INSERT INTO proveedores (nombre, contacto)
SELECT 'Lubricantes Andinos', 'Laura Rojas - 3005550101' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM proveedores WHERE nombre = 'Lubricantes Andinos');
INSERT INTO proveedores (nombre, contacto)
SELECT 'Electro Auto', 'Diego Ruiz - 3005550102' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM proveedores WHERE nombre = 'Electro Auto');

SET @motor = (SELECT MIN(id) FROM proveedores WHERE nombre = 'Repuestos El Motor');
SET @lubricantes = (SELECT MIN(id) FROM proveedores WHERE nombre = 'Lubricantes Andinos');
SET @electro = (SELECT MIN(id) FROM proveedores WHERE nombre = 'Electro Auto');
SET @mecanico = (SELECT id FROM repuesto_tipos WHERE nombre = 'Mecánico');
SET @electrico = (SELECT id FROM repuesto_tipos WHERE nombre = 'Eléctrico');

INSERT INTO repuestos
    (nombre, repuesto_tipo_id, proveedor_id, precio_compra, precio_venta, cantidad_stock, stock_minimo)
SELECT 'Aceite de motor 5W30', @mecanico, @lubricantes, 35000, 55000, 20, 5 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM repuestos WHERE nombre = 'Aceite de motor 5W30' AND proveedor_id = @lubricantes);
INSERT INTO repuestos
    (nombre, repuesto_tipo_id, proveedor_id, precio_compra, precio_venta, cantidad_stock, stock_minimo)
SELECT 'Filtro de aceite', @mecanico, @lubricantes, 12000, 22000, 12, 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM repuestos WHERE nombre = 'Filtro de aceite' AND proveedor_id = @lubricantes);
INSERT INTO repuestos
    (nombre, repuesto_tipo_id, proveedor_id, precio_compra, precio_venta, cantidad_stock, stock_minimo)
SELECT 'Bombillo H4', @electrico, @electro, 8000, 15000, 1, 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM repuestos WHERE nombre = 'Bombillo H4' AND proveedor_id = @electro);
INSERT INTO repuestos
    (nombre, repuesto_tipo_id, proveedor_id, precio_compra, precio_venta, cantidad_stock, stock_minimo)
SELECT 'Fusible 15A', @electrico, @electro, 1000, 2500, 0, 5 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM repuestos WHERE nombre = 'Fusible 15A' AND proveedor_id = @electro);
INSERT INTO repuestos
    (nombre, repuesto_tipo_id, proveedor_id, precio_compra, precio_venta, cantidad_stock, stock_minimo)
SELECT 'Refrigerante', @mecanico, @lubricantes, 18000, 30000, 8, 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM repuestos WHERE nombre = 'Refrigerante' AND proveedor_id = @lubricantes);

SET @aceite = (SELECT MIN(id) FROM repuestos WHERE nombre = 'Aceite de motor 5W30' AND proveedor_id = @lubricantes);
SET @filtro = (SELECT MIN(id) FROM repuestos WHERE nombre = 'Filtro de aceite' AND proveedor_id = @lubricantes);
SET @bombillo = (SELECT MIN(id) FROM repuestos WHERE nombre = 'Bombillo H4' AND proveedor_id = @electro);
SET @refrigerante = (SELECT MIN(id) FROM repuestos WHERE nombre = 'Refrigerante' AND proveedor_id = @lubricantes);

-- Lotes vigentes, uno próximo a vencer, uno vencido y otro sin caducidad.
INSERT INTO lotes (codigo_lote, repuesto_id, proveedor_id, fecha_ingreso, fecha_caducidad,
                   cantidad_recibida, cantidad_disponible)
SELECT 'DEMO-ACEITE-01', @aceite, @lubricantes, CURRENT_DATE - INTERVAL 30 DAY,
       CURRENT_DATE + INTERVAL 15 DAY, 20, 20 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM lotes WHERE codigo_lote = 'DEMO-ACEITE-01');
INSERT INTO lotes (codigo_lote, repuesto_id, proveedor_id, fecha_ingreso, fecha_caducidad,
                   cantidad_recibida, cantidad_disponible)
SELECT 'DEMO-FILTRO-01', @filtro, @lubricantes, CURRENT_DATE - INTERVAL 15 DAY,
       NULL, 12, 12 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM lotes WHERE codigo_lote = 'DEMO-FILTRO-01');
INSERT INTO lotes (codigo_lote, repuesto_id, proveedor_id, fecha_ingreso, fecha_caducidad,
                   cantidad_recibida, cantidad_disponible)
SELECT 'DEMO-BOMBILLO-01', @bombillo, @electro, CURRENT_DATE - INTERVAL 10 DAY,
       NULL, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM lotes WHERE codigo_lote = 'DEMO-BOMBILLO-01');
INSERT INTO lotes (codigo_lote, repuesto_id, proveedor_id, fecha_ingreso, fecha_caducidad,
                   cantidad_recibida, cantidad_disponible)
SELECT 'DEMO-REFRIGERANTE-01', @refrigerante, @lubricantes, CURRENT_DATE - INTERVAL 60 DAY,
       CURRENT_DATE - INTERVAL 5 DAY, 8, 8 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM lotes WHERE codigo_lote = 'DEMO-REFRIGERANTE-01');

INSERT INTO evaluaciones_proveedor
    (proveedor_id, fecha_evaluacion, calificacion_calidad, calificacion_puntualidad, observaciones)
SELECT @motor, CURRENT_DATE, 5, 4, 'DEMO: productos de buena calidad y entrega con un día de retraso' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM evaluaciones_proveedor WHERE proveedor_id = @motor
                  AND observaciones = 'DEMO: productos de buena calidad y entrega con un día de retraso');
INSERT INTO evaluaciones_proveedor
    (proveedor_id, fecha_evaluacion, calificacion_calidad, calificacion_puntualidad, observaciones)
SELECT @lubricantes, CURRENT_DATE, 5, 5, 'DEMO: entrega completa y puntual' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM evaluaciones_proveedor WHERE proveedor_id = @lubricantes
                  AND observaciones = 'DEMO: entrega completa y puntual');
INSERT INTO evaluaciones_proveedor
    (proveedor_id, fecha_evaluacion, calificacion_calidad, calificacion_puntualidad, observaciones)
SELECT @electro, CURRENT_DATE, 3, 2, 'DEMO: entrega tardía y empaque deteriorado' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM evaluaciones_proveedor WHERE proveedor_id = @electro
                  AND observaciones = 'DEMO: entrega tardía y empaque deteriorado');

-- Órdenes adicionales para practicar sin modificar las usadas en los reportes.
INSERT INTO servicios (nombre_servicio, vehiculo_id, mantenimiento_tipo_id, fecha_recepcion,
                       descripcion_problema, costo_mano_obra, estado_id)
SELECT 'Cambio de aceite', @v1, @preventivo, CURRENT_DATE,
       'DEMO integral: mantenimiento por kilometraje', 45000, @pendiente FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM servicios WHERE descripcion_problema = 'DEMO integral: mantenimiento por kilometraje');
INSERT INTO servicios (nombre_servicio, vehiculo_id, mantenimiento_tipo_id, fecha_recepcion,
                       descripcion_problema, costo_mano_obra, estado_id)
SELECT 'Revisión del sistema eléctrico', @v2, @correctivo, CURRENT_DATE,
       'DEMO integral: luz delantera intermitente', 30000, @proceso FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM servicios WHERE descripcion_problema = 'DEMO integral: luz delantera intermitente');
SET @demo_aceite = (SELECT MIN(id) FROM servicios WHERE descripcion_problema = 'DEMO integral: mantenimiento por kilometraje');
SET @demo_electrico = (SELECT MIN(id) FROM servicios WHERE descripcion_problema = 'DEMO integral: luz delantera intermitente');
INSERT IGNORE INTO servicios_tecnicos (servicio_id, tecnico_id)
VALUES (@demo_aceite, @luis), (@demo_electrico, @ana);

-- Estados de reserva para consultar desde inventario.
-- No reinicia las reservas si el programa ya cambió su estado.
INSERT INTO reservas_repuesto (repuesto_id, servicio_id, cantidad, estado, fecha_reserva)
SELECT @aceite, @demo_aceite, 2, 'RESERVADO', CURRENT_TIMESTAMP FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM reservas_repuesto WHERE repuesto_id = @aceite AND servicio_id = @demo_aceite);
INSERT INTO reservas_repuesto (repuesto_id, servicio_id, cantidad, estado, fecha_reserva)
SELECT @filtro, @demo_aceite, 1, 'RESERVADO', CURRENT_TIMESTAMP FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM reservas_repuesto WHERE repuesto_id = @filtro AND servicio_id = @demo_aceite);
INSERT INTO reservas_repuesto (repuesto_id, servicio_id, cantidad, estado, fecha_reserva)
SELECT @bateria, @demo_electrico, 1, 'CANCELADO', CURRENT_TIMESTAMP FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM reservas_repuesto WHERE repuesto_id = @bateria AND servicio_id = @demo_electrico);
INSERT INTO reservas_repuesto (repuesto_id, servicio_id, cantidad, estado, fecha_reserva)
SELECT @pastillas, @s2, 2, 'DESCONTADO', '2026-10-14 12:00:00' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM reservas_repuesto WHERE repuesto_id = @pastillas AND servicio_id = @s2);
INSERT INTO reservas_repuesto (repuesto_id, servicio_id, cantidad, estado, fecha_reserva)
SELECT @bateria, @s3, 1, 'DESCONTADO', '2026-10-20 14:00:00' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM reservas_repuesto WHERE repuesto_id = @bateria AND servicio_id = @s3);

-- Facturas de ejemplo para órdenes ya Facturado o Entregado.
-- Impuestos de demostración = 0; el total es repuestos + mano de obra.
INSERT INTO facturas (servicio_id, fecha_emision, total_repuestos, total_mano_obra, impuestos, total_final)
SELECT s.id, COALESCE(s.fecha_entrega, s.fecha_recepcion),
       COALESCE(consumos.total, 0), COALESCE(s.costo_mano_obra, 0), 0,
       COALESCE(consumos.total, 0) + COALESCE(s.costo_mano_obra, 0)
FROM servicios s
JOIN servicio_estados e ON e.id = s.estado_id
LEFT JOIN (
    SELECT servicio_id, SUM(cantidad_usada * precio_unitario_venta) AS total
    FROM servicios_repuestos GROUP BY servicio_id
) consumos ON consumos.servicio_id = s.id
WHERE s.descripcion_problema IN ('Reporte: cambio de aceite', 'Reporte: cambio de pastillas')
  AND e.nombre IN ('Facturado', 'Entregado')
  AND NOT EXISTS (SELECT 1 FROM facturas f WHERE f.servicio_id = s.id);

COMMIT;

-- Resumen de lo cargado, para comprobar que todas las tablas tengan datos.
SELECT 'clientes' AS tabla, COUNT(*) AS registros FROM clientes
UNION ALL SELECT 'vehiculos', COUNT(*) FROM vehiculos
UNION ALL SELECT 'proveedores', COUNT(*) FROM proveedores
UNION ALL SELECT 'repuestos', COUNT(*) FROM repuestos
UNION ALL SELECT 'repuesto_tipos', COUNT(*) FROM repuesto_tipos
UNION ALL SELECT 'mantenimiento_tipos', COUNT(*) FROM mantenimiento_tipos
UNION ALL SELECT 'servicio_estados', COUNT(*) FROM servicio_estados
UNION ALL SELECT 'servicios', COUNT(*) FROM servicios
UNION ALL SELECT 'tecnicos', COUNT(*) FROM tecnicos
UNION ALL SELECT 'servicios_tecnicos', COUNT(*) FROM servicios_tecnicos
UNION ALL SELECT 'servicios_repuestos', COUNT(*) FROM servicios_repuestos
UNION ALL SELECT 'lotes', COUNT(*) FROM lotes
UNION ALL SELECT 'evaluaciones_proveedor', COUNT(*) FROM evaluaciones_proveedor
UNION ALL SELECT 'reservas_repuesto', COUNT(*) FROM reservas_repuesto
UNION ALL SELECT 'facturas', COUNT(*) FROM facturas;

-- CONSULTAS PARA LA PRESENTACIÓN
-- Ejecutar schema.sql y después todo data.sql muestra las tablas y los resultados.
SHOW TABLES;

SELECT id, nombre FROM repuesto_tipos ORDER BY id;
SELECT id, nombre FROM mantenimiento_tipos ORDER BY id;
SELECT id, nombre FROM servicio_estados ORDER BY id;
SELECT id, nombre, documento_identidad, telefono, email FROM clientes ORDER BY id;

SELECT v.id, v.placa, v.marca, v.modelo, c.nombre AS cliente
FROM vehiculos v LEFT JOIN clientes c ON c.id = v.cliente_id ORDER BY v.id;

SELECT id, nombre, contacto FROM proveedores ORDER BY id;

SELECT r.id, r.nombre, t.nombre AS tipo, p.nombre AS proveedor,
       r.precio_compra, r.precio_venta, r.cantidad_stock, r.stock_minimo
FROM repuestos r
LEFT JOIN repuesto_tipos t ON t.id = r.repuesto_tipo_id
LEFT JOIN proveedores p ON p.id = r.proveedor_id
ORDER BY r.id;

SELECT id, nombre, cantidad_stock, stock_minimo
FROM repuestos WHERE cantidad_stock < stock_minimo ORDER BY id;

SELECT l.codigo_lote, r.nombre AS repuesto, p.nombre AS proveedor,
       l.fecha_ingreso, l.fecha_caducidad, l.cantidad_recibida, l.cantidad_disponible
FROM lotes l
JOIN repuestos r ON r.id = l.repuesto_id
JOIN proveedores p ON p.id = l.proveedor_id
ORDER BY l.id;

SELECT l.codigo_lote, r.nombre AS repuesto, l.fecha_caducidad, l.cantidad_disponible
FROM lotes l JOIN repuestos r ON r.id = l.repuesto_id
WHERE l.fecha_caducidad <= CURRENT_DATE + INTERVAL 30 DAY
  AND l.cantidad_disponible > 0
ORDER BY l.fecha_caducidad;

SELECT e.id, p.nombre AS proveedor, e.fecha_evaluacion,
       e.calificacion_calidad, e.calificacion_puntualidad,
       (e.calificacion_calidad + e.calificacion_puntualidad) / 2.0 AS promedio,
       e.observaciones
FROM evaluaciones_proveedor e JOIN proveedores p ON p.id = e.proveedor_id
ORDER BY e.id;

SELECT s.id AS orden, s.nombre_servicio, v.placa, c.nombre AS cliente,
       mt.nombre AS mantenimiento, e.nombre AS estado, s.descripcion_problema,
       s.costo_mano_obra, s.fecha_recepcion, s.fecha_entrega
FROM servicios s
LEFT JOIN vehiculos v ON v.id = s.vehiculo_id
LEFT JOIN clientes c ON c.id = v.cliente_id
LEFT JOIN mantenimiento_tipos mt ON mt.id = s.mantenimiento_tipo_id
LEFT JOIN servicio_estados e ON e.id = s.estado_id
ORDER BY s.id;

SELECT id, nombre FROM tecnicos ORDER BY id;
SELECT st.servicio_id AS orden, t.id AS tecnico_id, t.nombre AS tecnico
FROM servicios_tecnicos st JOIN tecnicos t ON t.id = st.tecnico_id
ORDER BY st.servicio_id, t.id;

SELECT rr.id, rr.servicio_id AS orden, r.nombre AS repuesto, rr.cantidad,
       rr.precio_unitario_venta, rr.estado, rr.fecha_reserva
FROM reservas_repuesto rr JOIN repuestos r ON r.id = rr.repuesto_id
ORDER BY rr.servicio_id, rr.id;

SELECT r.id, r.nombre, r.cantidad_stock,
       COALESCE(reservas.cantidad, 0) AS cantidad_reservada,
       r.cantidad_stock - COALESCE(reservas.cantidad, 0) AS cantidad_disponible
FROM repuestos r
LEFT JOIN (
    SELECT repuesto_id, SUM(cantidad) AS cantidad
    FROM reservas_repuesto WHERE estado = 'RESERVADO' GROUP BY repuesto_id
) reservas ON reservas.repuesto_id = r.id
ORDER BY r.id;

SELECT sr.servicio_id AS orden, r.nombre AS repuesto, sr.cantidad_usada,
       sr.precio_unitario_venta,
       sr.cantidad_usada * sr.precio_unitario_venta AS subtotal
FROM servicios_repuestos sr JOIN repuestos r ON r.id = sr.repuesto_id
ORDER BY sr.servicio_id, r.id;

SELECT f.id AS factura, f.servicio_id AS orden, c.nombre AS cliente, v.placa,
       f.fecha_emision, f.total_repuestos, f.total_mano_obra, f.impuestos, f.total_final
FROM facturas f
JOIN servicios s ON s.id = f.servicio_id
LEFT JOIN vehiculos v ON v.id = s.vehiculo_id
LEFT JOIN clientes c ON c.id = v.cliente_id
ORDER BY f.id;

SELECT COALESCE(NULLIF(TRIM(nombre_servicio), ''), 'Sin nombre registrado') AS servicio,
       COUNT(*) AS solicitudes
FROM servicios GROUP BY COALESCE(NULLIF(TRIM(nombre_servicio), ''), 'Sin nombre registrado')
ORDER BY solicitudes DESC, servicio;

SELECT t.id, t.nombre, COUNT(st.servicio_id) AS ordenes_asignadas,
       SUM(CASE WHEN e.nombre IN ('Completado', 'Facturado', 'Entregado') THEN 1 ELSE 0 END) AS terminadas
FROM tecnicos t
LEFT JOIN servicios_tecnicos st ON st.tecnico_id = t.id
LEFT JOIN servicios s ON s.id = st.servicio_id
LEFT JOIN servicio_estados e ON e.id = s.estado_id
GROUP BY t.id, t.nombre ORDER BY terminadas DESC, t.nombre;

SELECT r.id, r.nombre, SUM(sr.cantidad_usada) AS unidades_consumidas,
       SUM(sr.cantidad_usada * sr.precio_unitario_venta) AS importe
FROM servicios_repuestos sr JOIN repuestos r ON r.id = sr.repuesto_id
GROUP BY r.id, r.nombre ORDER BY unidades_consumidas DESC, r.nombre;