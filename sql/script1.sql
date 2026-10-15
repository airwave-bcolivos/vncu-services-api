-- 1. Habilitar extensión para generar UUIDs automáticamente
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Tabla de Tipos de Documento (Catálogo)
CREATE TABLE document_type (
    document_type_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT true
);

-- Insertar datos básicos por defecto
INSERT INTO document_type (name, code) VALUES ('Documento Nacional de Identidad', 'DNI');
INSERT INTO document_type (name, code) VALUES ('Carnet de Extranjería', 'CE');

-- 3. Tabla Central de Usuarios
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- Valores esperados: CLIENT, TECHNICIAN, ADMIN
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tabla de Perfiles de Cliente
CREATE TABLE client_profiles (
    user_id UUID PRIMARY KEY,
    names VARCHAR(150) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    second_lastname VARCHAR(100), -- Opcional (Nullable)
    document_type_id UUID NOT NULL,
    document_id VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    address VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_client_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_client_doc_type FOREIGN KEY (document_type_id) REFERENCES document_type (document_type_id),
    CONSTRAINT uq_client_document UNIQUE (document_id) -- Evita DNIs duplicados
);

-- 5. Tabla de Perfiles de Técnico
CREATE TABLE technician_profiles (
    user_id UUID PRIMARY KEY,
    names VARCHAR(150) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    second_lastname VARCHAR(100), -- Opcional (Nullable)
    document_type_id UUID NOT NULL,
    document_id VARCHAR(50) NOT NULL,
    specialty VARCHAR(100) NOT NULL, -- Renombrado para no hacer conflicto con el 'role' de users
    bio_description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_technician_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_technician_doc_type FOREIGN KEY (document_type_id) REFERENCES document_type (document_type_id),
    CONSTRAINT uq_technician_document UNIQUE (document_id) -- Evita DNIs duplicados
);