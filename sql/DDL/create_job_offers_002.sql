-- 6. Tablas de Ubigeo (Estándar INEI Perú)
CREATE TABLE ubigeo_departments (
                                    id VARCHAR(2) PRIMARY KEY,
                                    name VARCHAR(100) NOT NULL
);

CREATE TABLE ubigeo_provinces (
                                  id VARCHAR(4) PRIMARY KEY,
                                  department_id VARCHAR(2) NOT NULL,
                                  name VARCHAR(100) NOT NULL,
                                  CONSTRAINT fk_province_department FOREIGN KEY (department_id) REFERENCES ubigeo_departments (id)
);

CREATE TABLE ubigeo_districts (
                                  id VARCHAR(6) PRIMARY KEY,
                                  province_id VARCHAR(4) NOT NULL,
                                  name VARCHAR(100) NOT NULL,
                                  CONSTRAINT fk_district_province FOREIGN KEY (province_id) REFERENCES ubigeo_provinces (id)
);

-- 7. Tabla de Ofertas de Trabajo (Publicaciones del MVP)
CREATE TABLE job_offers (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            client_id UUID NOT NULL,
                            title VARCHAR(150) NOT NULL,
                            specialty VARCHAR(100) NOT NULL,
                            description TEXT NOT NULL,
                            max_budget DECIMAL(10, 2) NOT NULL,
                            status VARCHAR(30) DEFAULT 'OPEN' NOT NULL, -- OPEN, IN_PROGRESS, COMPLETED, CANCELED
                            technician_id UUID,
                            ubigeo_district_id VARCHAR(6) NOT NULL,
                            specific_address VARCHAR(255) NOT NULL,
                            latitude DECIMAL(10, 8) NOT NULL,
                            longitude DECIMAL(11, 8) NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_job_client FOREIGN KEY (client_id) REFERENCES client_profiles (user_id) ON DELETE CASCADE,
                            CONSTRAINT fk_job_technician FOREIGN KEY (technician_id) REFERENCES technician_profiles (user_id) ON DELETE SET NULL,
                            CONSTRAINT fk_job_ubigeo FOREIGN KEY (ubigeo_district_id) REFERENCES ubigeo_districts (id)
);

-- 8. Índices de optimización
CREATE INDEX idx_job_offers_specialty ON job_offers(specialty);
CREATE INDEX idx_job_offers_status ON job_offers(status);
CREATE INDEX idx_job_offers_district ON job_offers(ubigeo_district_id);