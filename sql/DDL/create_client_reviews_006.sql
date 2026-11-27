CREATE TABLE IF NOT EXISTS public.client_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_offer_id UUID NOT NULL REFERENCES public.job_offers (id) ON DELETE CASCADE,
    client_id UUID NOT NULL REFERENCES public.client_profiles (user_id) ON DELETE CASCADE,
    rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_client_review_job_offer UNIQUE (job_offer_id)
);

CREATE INDEX IF NOT EXISTS idx_client_reviews_client ON public.client_reviews (client_id);
