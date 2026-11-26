CREATE TABLE IF NOT EXISTS diagnostic_offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_offer_id UUID NOT NULL,
    technician_id UUID NOT NULL,
    diagnostic_price DECIMAL(10, 2) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_diag_offer_job FOREIGN KEY (job_offer_id) REFERENCES job_offers (id) ON DELETE CASCADE,
    CONSTRAINT fk_diag_offer_technician FOREIGN KEY (technician_id) REFERENCES technician_profiles (user_id) ON DELETE CASCADE,
    CONSTRAINT uq_job_technician_offer UNIQUE (job_offer_id, technician_id)
);

CREATE INDEX IF NOT EXISTS idx_diagnostic_offers_job ON diagnostic_offers (job_offer_id);
CREATE INDEX IF NOT EXISTS idx_diagnostic_offers_tech ON diagnostic_offers (technician_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_job_accepted_diagnostic_offer
    ON diagnostic_offers (job_offer_id) WHERE status = 'ACCEPTED';
