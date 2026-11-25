CREATE DATABASE IF NOT EXISTS carmotors CHARACTER SET utf8mb4;
USE carmotors;

CREATE TABLE IF NOT EXISTS repuesto_tipos (id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) UNIQUE NOT NULL);
CREATE TABLE IF NOT EXISTS servicio_estados (id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) UNIQUE NOT NULL);
CREATE TABLE IF NOT EXISTS mantenimiento_tipos (id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) UNIQUE NOT NULL);


CREATE TABLE IF NOT EXISTS clientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    documento_identidad VARCHAR(20) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS vehiculos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT,
    placa VARCHAR(10) UNIQUE NOT NULL,
    marca VARCHAR(50),
    modelo VARCHAR(50),
    FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE TABLE IF NOT EXISTS proveedores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    contacto VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS repuestos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    repuesto_tipo_id INT,
    proveedor_id INT,
    precio_compra DECIMAL(10, 2),
    precio_venta DECIMAL(10, 2),
    cantidad_stock INT NOT NULL,
    stock_minimo INT NOT NULL,
    FOREIGN KEY (repuesto_tipo_id) REFERENCES repuesto_tipos(id),
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
);

CREATE TABLE IF NOT EXISTS servicios (
    nombre_servicio VARCHAR(100),
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehiculo_id INT,
    mantenimiento_tipo_id INT,
    fecha_recepcion DATETIME NOT NULL,
    fecha_entrega DATETIME,
    descripcion_problema TEXT,
    costo_mano_obra DECIMAL(10, 2),
    estado_id INT,
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    FOREIGN KEY (mantenimiento_tipo_id) REFERENCES mantenimiento_tipos(id),
    FOREIGN KEY (estado_id) REFERENCES servicio_estados(id)
);

CREATE TABLE IF NOT EXISTS servicios_repuestos ( -- Tabla de unión para la relación muchos-a-muchos
    servicio_id INT,
    repuesto_id INT,
    cantidad_usada INT NOT NULL,
    precio_unitario_venta DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (servicio_id, repuesto_id),
    FOREIGN KEY (servicio_id) REFERENCES servicios(id),
    FOREIGN KEY (repuesto_id) REFERENCES repuestos(id)
);

CREATE TABLE IF NOT EXISTS facturas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    servicio_id INT,
    fecha_emision DATETIME NOT NULL,
    total_repuestos DECIMAL(10, 2),
    total_mano_obra DECIMAL(10, 2),
    impuestos DECIMAL(10, 2),
    total_final DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (servicio_id) REFERENCES servicios(id)
);

CREATE TABLE IF NOT EXISTS tecnicos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS servicios_tecnicos (
    servicio_id INT NOT NULL,
    tecnico_id INT NOT NULL,
    PRIMARY KEY (servicio_id, tecnico_id),
    FOREIGN KEY (servicio_id) REFERENCES servicios(id),
    FOREIGN KEY (tecnico_id) REFERENCES tecnicos(id)
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS lotes (
                                     id                  INT AUTO_INCREMENT PRIMARY KEY,
                                     codigo_lote         VARCHAR(50)  NOT NULL UNIQUE,
    repuesto_id         INT          NOT NULL,
    proveedor_id        INT          NOT NULL,
    fecha_ingreso       DATE         NOT NULL,
    fecha_caducidad     DATE         NULL,
    cantidad_recibida   INT          NOT NULL,
    cantidad_disponible INT          NOT NULL,
    FOREIGN KEY (repuesto_id)  REFERENCES repuestos(id),
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    INDEX idx_lotes_repuesto (repuesto_id),
    INDEX idx_lotes_caducidad (fecha_caducidad)
);

CREATE TABLE IF NOT EXISTS evaluaciones_proveedor (
                                                      id                       INT AUTO_INCREMENT PRIMARY KEY,
                                                      proveedor_id             INT  NOT NULL,
                                                      fecha_evaluacion         DATE NOT NULL,
                                                      calificacion_calidad     TINYINT NOT NULL,
                                                      calificacion_puntualidad TINYINT NOT NULL,
                                                      observaciones            TEXT,
                                                      FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    INDEX idx_evaluaciones_proveedor (proveedor_id)
);

CREATE TABLE IF NOT EXISTS reservas_repuesto (
                                                 id             INT AUTO_INCREMENT PRIMARY KEY,
                                                 repuesto_id    INT NOT NULL,
                                                 servicio_id    INT NOT NULL,
                                                 cantidad       INT NOT NULL,
                                                 estado         VARCHAR(20) NOT NULL DEFAULT 'RESERVADO',
    precio_unitario_venta DECIMAL(10,2),
    fecha_reserva  DATETIME NOT NULL,
    FOREIGN KEY (repuesto_id) REFERENCES repuestos(id),
    INDEX idx_reservas_servicio (servicio_id),
    INDEX idx_reservas_estado (estado)
);

-- Actualización para bases creadas antes del nombre del servicio.
-- Agrega el nombre sin eliminar las órdenes existentes.
SET @existe_nombre = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'servicios'
      AND column_name = 'nombre_servicio'
);
SET @sql_nombre = IF(@existe_nombre = 0,
    'ALTER TABLE servicios ADD COLUMN nombre_servicio VARCHAR(100)',
    'SELECT ''La columna nombre_servicio ya existe'' AS resultado');
PREPARE actualizar_nombre FROM @sql_nombre;
EXECUTE actualizar_nombre;
DEALLOCATE PREPARE actualizar_nombre;


-- Conserva el precio al reservar, también en bases creadas anteriormente.
SET @existe_precio_reserva = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'reservas_repuesto'
      AND column_name = 'precio_unitario_venta'
);
SET @sql_precio_reserva = IF(@existe_precio_reserva = 0,
    'ALTER TABLE reservas_repuesto ADD COLUMN precio_unitario_venta DECIMAL(10,2)',
    'SELECT ''La columna de precio de reserva ya existe'' AS resultado');
PREPARE actualizar_precio_reserva FROM @sql_precio_reserva;
EXECUTE actualizar_precio_reserva;
DEALLOCATE PREPARE actualizar_precio_reserva;