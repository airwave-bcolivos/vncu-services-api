ALTER TABLE public.client_profiles
    ADD COLUMN IF NOT EXISTS average_rating numeric(3,2),
    ADD COLUMN IF NOT EXISTS rating_count integer NOT NULL DEFAULT 0;

ALTER TABLE public.client_profiles
    ADD CONSTRAINT ck_client_avg_rating
    CHECK (average_rating IS NULL OR average_rating BETWEEN 1 AND 5);

CREATE INDEX IF NOT EXISTS idx_client_average_rating
    ON public.client_profiles (average_rating DESC NULLS LAST);