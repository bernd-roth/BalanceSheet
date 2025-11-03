-- Migration: Add taxable column to incomeexpense table
-- Created: 2025-01-XX
-- Description: Adds a boolean taxable field to track whether an expense should be included in tax calculations
--              For properties like Hollgasse 1/54 with multiple insurances where only some are tax-deductible

-- Add the taxable column with a default value of FALSE
ALTER TABLE incomeexpense
ADD COLUMN IF NOT EXISTS taxable BOOLEAN DEFAULT FALSE;

-- Update the column to NOT NULL after setting the default
-- (Existing rows will have FALSE, new rows will use the default)
ALTER TABLE incomeexpense
ALTER COLUMN taxable SET NOT NULL;

-- Optional: Add a comment to document the column's purpose
COMMENT ON COLUMN incomeexpense.taxable IS
'Indicates whether this expense/income is taxable. Used to separate entries for property-specific Excel reports. For example, for Hollgasse 1/54, taxable=true entries go to Hollgasse_1 report, taxable=false entries go to Hollgasse_54 report.';

-- Verify the migration
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'incomeexpense' AND column_name = 'taxable';
