-- public.transaction_log definition

-- Drop table

-- DROP TABLE public.transaction_log;

CREATE TABLE public.transaction_log (
	transaction_id varchar(50) NOT NULL,
	"timestamp" timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	processed bool DEFAULT false NULL,
	CONSTRAINT transaction_log_pkey PRIMARY KEY (transaction_id)
);