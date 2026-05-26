-- ============================================================
-- Hospital Universitario San Rafael
-- Script DDL para PostgreSQL
-- ============================================================

-- Crear la base de datos (ejecutar como superusuario)
-- CREATE DATABASE hospital_san_rafael;
-- \c hospital_san_rafael

-- ============================================================
-- TABLAS
-- ============================================================

CREATE TABLE IF NOT EXISTS universidad (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    ciudad VARCHAR(100),
    estado BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS especialidad (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    sede VARCHAR(100),
    capacidad_maxima INT DEFAULT 5
);

CREATE TABLE IF NOT EXISTS tutor (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    cedula VARCHAR(50),
    rol VARCHAR(100),
    correo VARCHAR(150),
    universidad_id BIGINT REFERENCES universidad(id)
);

CREATE TABLE IF NOT EXISTS estudiante (
    id BIGSERIAL PRIMARY KEY,
    nombres VARCHAR(150) NOT NULL,
    apellidos VARCHAR(150) NOT NULL,
    documento VARCHAR(50) NOT NULL UNIQUE,
    estado_civil VARCHAR(50),
    fecha_nacimiento DATE,
    lugar_nacimiento VARCHAR(100),
    direccion_tunja VARCHAR(200),
    celular VARCHAR(20),
    correo VARCHAR(150),
    programa VARCHAR(150),
    fecha_ingreso DATE,
    semestre INT,
    universidad_id BIGINT REFERENCES universidad(id),
    promedio DECIMAL(4,2),
    tipo_vinculacion VARCHAR(100) DEFAULT 'Estudiante en práctica',
    grupo_sanguineo VARCHAR(10),
    induccion_completa BOOLEAN DEFAULT FALSE,
    vacunas_completas BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    -- Rutas de archivos adjuntos
    foto_path VARCHAR(255),
    documento_soporte_path VARCHAR(255),
    documento_vacunas_path VARCHAR(255),
    certificado_cursos_path VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS programacion (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL REFERENCES estudiante(id),
    mes INT NOT NULL,
    anio INT NOT NULL,
    tutor_universidad_id BIGINT REFERENCES tutor(id),
    tutor_hospital_id BIGINT REFERENCES tutor(id),
    UNIQUE (estudiante_id, mes, anio)
);

CREATE TABLE IF NOT EXISTS programacion_detalle (
    id BIGSERIAL PRIMARY KEY,
    programacion_id BIGINT NOT NULL REFERENCES programacion(id),
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    especialidad_id BIGINT NOT NULL REFERENCES especialidad(id)
);

CREATE TABLE IF NOT EXISTS acceso (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL REFERENCES estudiante(id),
    fecha DATE NOT NULL,
    hora_ingreso TIMESTAMP,
    hora_salida TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'FUERA',
    especialidad_id BIGINT REFERENCES especialidad(id)
);

CREATE TABLE IF NOT EXISTS usuario_sistema (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    cedula VARCHAR(50) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    correo VARCHAR(150),
    activo BOOLEAN DEFAULT TRUE
);

-- ============================================================
-- MIGRACIÓN: agregar columnas a BD existente
-- (Ejecutar solo si la tabla estudiante ya existe sin estas columnas)
-- ============================================================
-- ALTER TABLE estudiante ADD COLUMN IF NOT EXISTS foto_path VARCHAR(255);
-- ALTER TABLE estudiante ADD COLUMN IF NOT EXISTS documento_soporte_path VARCHAR(255);
-- ALTER TABLE estudiante ADD COLUMN IF NOT EXISTS documento_vacunas_path VARCHAR(255);
-- ALTER TABLE estudiante ADD COLUMN IF NOT EXISTS certificado_cursos_path VARCHAR(255);

-- ============================================================
-- ÍNDICES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_estudiante_documento ON estudiante(documento);
CREATE INDEX IF NOT EXISTS idx_acceso_estado ON acceso(estado);
CREATE INDEX IF NOT EXISTS idx_acceso_fecha ON acceso(fecha);
CREATE INDEX IF NOT EXISTS idx_programacion_mes_anio ON programacion(mes, anio);
CREATE INDEX IF NOT EXISTS idx_usuario_username ON usuario_sistema(username);

-- ============================================================
-- DATOS INICIALES
-- ============================================================
-- Usuario: admin | Contraseña: admin123
-- INSERT INTO usuario_sistema (nombre, username, password, cedula, rol)
-- VALUES ('Administrador del Sistema', 'admin',
--         '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
--         '00000000', 'ADMINISTRADOR');