CREATE TABLE IF NOT EXISTS public.job_reviews
(
    id             uuid NOT NULL DEFAULT gen_random_uuid(),
    job_offer_id   uuid NOT NULL,
    technician_id  uuid NOT NULL,
    rating         smallint NOT NULL,
    comment        text,
    created_at     timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT job_reviews_pkey PRIMARY KEY (id),
    CONSTRAINT uq_review_job_offer UNIQUE (job_offer_id),
    CONSTRAINT ck_review_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT fk_review_job_offer FOREIGN KEY (job_offer_id)
        REFERENCES public.job_offers (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_technician FOREIGN KEY (technician_id)
        REFERENCES public.users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_job_reviews_technician
    ON public.job_reviews (technician_id);

GRANT ALL ON TABLE public.job_reviews TO postgres;
GRANT ALL ON TABLE public.job_reviews TO salva;