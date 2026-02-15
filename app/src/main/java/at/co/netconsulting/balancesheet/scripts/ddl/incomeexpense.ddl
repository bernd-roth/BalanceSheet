-- public.incomeexpense definition

-- Drop table

-- DROP TABLE public.incomeexpense;

CREATE TABLE public.incomeexpense (
	id serial4 NOT NULL,
	orderdate date NULL,
	who varchar(255) NULL,
	"position" varchar(255) NULL,
	income numeric(16, 2) NULL,
	expense numeric(16, 2) NULL,
	"location" varchar NULL,
	"comment" varchar NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	taxable bool DEFAULT false NOT NULL,
	export_to varchar(50) DEFAULT 'auto'::character varying NOT NULL,
	is_info_only bool DEFAULT false NOT NULL,
	original_income numeric(16, 2) NULL,
	original_expense numeric(16, 2) NULL,
	original_currency varchar(10) NULL,
	CONSTRAINT incomeexpense_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_incomeexpense_export_to ON public.incomeexpense USING btree (export_to);