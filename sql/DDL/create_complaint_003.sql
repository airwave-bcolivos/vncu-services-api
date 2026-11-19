-- Table: public.complaint

-- DROP TABLE IF EXISTS public.complaint;

CREATE TABLE IF NOT EXISTS public.complaint
(
    complaint_id uuid NOT NULL DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL,
    title character varying(150) COLLATE pg_catalog."default" NOT NULL,
    description character varying(300) COLLATE pg_catalog."default" NOT NULL,
    status character varying(30) COLLATE pg_catalog."default" NOT NULL DEFAULT 'RECEIVED'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT complaint_pkey PRIMARY KEY (complaint_id),
    CONSTRAINT fk_complaint_user FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,  -- si prefieres conservar los reclamos, cambia a RESTRICT
    CONSTRAINT chk_complaint_status
        CHECK (status IN ('RECEIVED', 'IN_REVIEW', 'RESOLVED', 'REJECTED'))
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.complaint
    OWNER to postgres;

GRANT ALL ON TABLE public.complaint TO postgres;
GRANT ALL ON TABLE public.complaint TO salva;

-- Index: idx_complaints_user_id
CREATE INDEX IF NOT EXISTS idx_complaint_user_id
    ON public.complaint USING btree
    (user_id ASC NULLS LAST);

-- Index: idx_complaints_status
CREATE INDEX IF NOT EXISTS idx_complaint_status
    ON public.complaint USING btree
    (status COLLATE pg_catalog."default" ASC NULLS LAST);

-- Index: idx_complaints_created_at
CREATE INDEX IF NOT EXISTS idx_complaint_created_at
    ON public.complaint USING btree
    (created_at DESC NULLS LAST);