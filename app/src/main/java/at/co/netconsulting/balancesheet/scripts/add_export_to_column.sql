-- Migration script to add export_to column to incomeexpense table
-- This allows entries to specify which export reports they should appear in

-- Add the export_to column with default value 'auto'
ALTER TABLE incomeexpense
ADD COLUMN IF NOT EXISTS export_to VARCHAR DEFAULT 'auto';

-- Create an index for better query performance when filtering by export_to
CREATE INDEX IF NOT EXISTS idx_incomeexpense_export_to
ON incomeexpense(export_to);

-- Update any existing NULL values to 'auto' (though the default should handle this)
UPDATE incomeexpense
SET export_to = 'auto'
WHERE export_to IS NULL;

-- Add a comment to document the column
COMMENT ON COLUMN incomeexpense.export_to IS
'Controls which export reports this entry appears in: auto, hollgasse, arbeitnehmerveranlagung, both, or none';
