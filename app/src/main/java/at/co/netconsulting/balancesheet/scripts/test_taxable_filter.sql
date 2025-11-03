-- Test query to verify taxable filtering works correctly

-- Show ALL Versicherung entries for Hollgasse 1/54
SELECT id, orderdate, who, position, expense, comment, taxable
FROM incomeexpense
WHERE location = 'Hollgasse 1/54'
AND position = 'Versicherung'
AND EXTRACT(YEAR FROM orderdate) = 2025
ORDER BY orderdate DESC;

-- Show ONLY taxable=true entries
SELECT id, orderdate, who, position, expense, comment, taxable
FROM incomeexpense
WHERE location = 'Hollgasse 1/54'
AND position = 'Versicherung'
AND EXTRACT(YEAR FROM orderdate) = 2025
AND taxable = true
ORDER BY orderdate DESC;

-- Show ONLY taxable=false entries
SELECT id, orderdate, who, position, expense, comment, taxable
FROM incomeexpense
WHERE location = 'Hollgasse 1/54'
AND position = 'Versicherung'
AND EXTRACT(YEAR FROM orderdate) = 2025
AND taxable = false
ORDER BY orderdate DESC;
