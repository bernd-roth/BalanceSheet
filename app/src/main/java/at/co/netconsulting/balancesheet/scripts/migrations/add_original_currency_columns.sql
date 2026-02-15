-- Migration: Add original currency columns to incomeexpense table
-- Description: Preserves the original income/expense amounts and currency code
--              before conversion to the default currency. NULL when no conversion occurred.

-- Add the original_income column
ALTER TABLE incomeexpense
ADD COLUMN IF NOT EXISTS original_income NUMERIC(16, 2) NULL;

-- Add the original_expense column
ALTER TABLE incomeexpense
ADD COLUMN IF NOT EXISTS original_expense NUMERIC(16, 2) NULL;

-- Add the original_currency column
ALTER TABLE incomeexpense
ADD COLUMN IF NOT EXISTS original_currency VARCHAR(10) NULL;

-- Add comments to document the columns' purpose
COMMENT ON COLUMN incomeexpense.original_income IS
'The income amount as originally entered by the user before currency conversion. NULL if no conversion occurred.';

COMMENT ON COLUMN incomeexpense.original_expense IS
'The expense amount as originally entered by the user before currency conversion. NULL if no conversion occurred.';

COMMENT ON COLUMN incomeexpense.original_currency IS
'The local currency code (e.g. SEK) of the original entry before conversion. NULL if no conversion occurred.';

-- Verify the migration
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'incomeexpense' AND column_name IN ('original_income', 'original_expense', 'original_currency');
