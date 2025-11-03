# Taxable Filtering Guide

## Overview
The `hollgasse.py` script now automatically filters entries based on the taxable field using intelligent location name parsing.

---

## Command Format (Unchanged!)

Your existing command **works without modification**:

```bash
cd /home/bernd/docker-container/balancesheet && \
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54 >> \
/home/bernd/docker-container/balancesheet/logs/hollgasse_1_54.log 2>&1 && \
/usr/bin/docker cp $(/usr/bin/docker-compose ps -q web):/app/hollgasse_1_54_$(date +\%Y).xlsx \
/mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx
```

**Default behavior:** Filters for **taxable=true** entries (Hollgasse 1 tax report)

---

## Location Name Conventions

| Command | Taxable Filter | Output File | Description |
|---------|----------------|-------------|-------------|
| `hollgasse.py Hollgasse_1_54` | `true` | `hollgasse_1_54_2025.xlsx` | **Taxable entries only** (default) |
| `hollgasse.py Hollgasse_1_54_nontaxable` | `false` | `hollgasse_1_54_nontaxable_2025.xlsx` | **Non-taxable entries** |
| `hollgasse.py Hollgasse_1_54_all` | `None` | `hollgasse_1_54_all_2025.xlsx` | **All entries** |

---

## Setup for Two Reports

### 1. Hollgasse 1 Report (Taxable Entries)
**Your existing command - no changes needed!**

```bash
cd /home/bernd/docker-container/balancesheet && \
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54 >> \
/home/bernd/docker-container/balancesheet/logs/hollgasse_1_54.log 2>&1 && \
/usr/bin/docker cp $(/usr/bin/docker-compose ps -q web):/app/hollgasse_1_54_$(date +\%Y).xlsx \
/mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx
```

**Includes:**
- ✅ Entry 5913: Rechtsschutzversicherung 37.79€ (taxable=true)
- ❌ Entry 5914: Rechtsschutzversicherung 35.72€ (taxable=false) - EXCLUDED

---

### 2. Hollgasse 54 Report (Non-Taxable Entries)
**Add this new command:**

```bash
cd /home/bernd/docker-container/balancesheet && \
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54_nontaxable >> \
/home/bernd/docker-container/balancesheet/logs/hollgasse_1_54_nontaxable.log 2>&1 && \
/usr/bin/docker cp $(/usr/bin/docker-compose ps -q web):/app/hollgasse_1_54_nontaxable_$(date +\%Y).xlsx \
/mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_nontaxable_$(date +\%Y).xlsx
```

**Includes:**
- ❌ Entry 5913: Rechtsschutzversicherung 37.79€ (taxable=true) - EXCLUDED
- ✅ Entry 5914: Rechtsschutzversicherung 35.72€ (taxable=false)
- ✅ Entry 5915: Freizeit 20.00€ (taxable=false)
- ✅ Entry 5916-5918: Other non-taxable entries

---

## einnahmen_ausgaben.xls

**No changes needed!** This script includes **ALL** entries regardless of taxable status:
- ✅ Entry 5913 (taxable=true)
- ✅ Entry 5914 (taxable=false)
- ✅ All other entries

---

## Log Output

When you run the script, you'll see:

### For Hollgasse 1 (taxable):
```
INFO: Filtering for TAXABLE entries only (taxable=True) - default behavior
Generating report for location: Hollgasse_1_54, year: 2025, taxable: True
Database location format: Hollgasse 1/54
Fetching data from database...
Found X total records

DEBUG - Fetched entries with taxable status:
  ID 5913: Versicherung | 37.79€ | taxable=True
  (only taxable=True entries shown)
```

### For Hollgasse 54 (non-taxable):
```
INFO: Filtering for NON-TAXABLE entries only (taxable=False)
Generating report for location: Hollgasse_1_54, year: 2025, taxable: False
Database location format: Hollgasse 1/54
Fetching data from database...
Found X total records

DEBUG - Fetched entries with taxable status:
  ID 5914: Versicherung | 35.72€ | taxable=False
  ID 5915: Freizeit | 20.00€ | taxable=False
  ID 5916-5918: Other entries with taxable=False
```

---

## Cron Job / Automation

You can schedule both reports:

```bash
# Daily at 2am - Hollgasse 1 (taxable)
0 2 * * * cd /home/bernd/docker-container/balancesheet && /usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54 >> /home/bernd/docker-container/balancesheet/logs/hollgasse_1_54.log 2>&1

# Daily at 2:05am - Hollgasse 54 (non-taxable)
5 2 * * * cd /home/bernd/docker-container/balancesheet && /usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54_nontaxable >> /home/bernd/docker-container/balancesheet/logs/hollgasse_1_54_nontaxable.log 2>&1
```

---

## Testing

To verify the filtering works correctly:

```bash
# Test taxable filter (should only show entry 5913)
docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54

# Test non-taxable filter (should only show entries 5914-5918)
docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54_nontaxable

# Test all entries (should show everything)
docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54_all
```

Check the DEBUG output in the logs to confirm correct filtering.

---

## Summary

✅ **Your existing command unchanged** - now filters for taxable=true by default
✅ **Add `_nontaxable` suffix** for non-taxable entries report
✅ **einnahmen_ausgaben.xls** includes ALL entries (no changes)
✅ **Two separate Excel files** for different tax purposes
✅ **No command-line parameter changes needed**
