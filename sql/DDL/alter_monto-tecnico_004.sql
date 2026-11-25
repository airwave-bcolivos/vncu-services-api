ALTER TABLE public.technician_profiles
ADD COLUMN consultation_fee numeric(10,2) NOT NULL DEFAULT 0.00;

ALTER TABLE public.technician_profiles
ADD CONSTRAINT ck_technician_consultation_fee CHECK (consultation_fee >= 0::numeric);