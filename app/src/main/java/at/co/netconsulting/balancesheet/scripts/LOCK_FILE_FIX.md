# Excel Lock File Issue - Complete Fix

## Problem
Error: "Diese Datei wurde von einem anderen Benutzer gesperrt"
(This file was locked by another user)

## Root Cause
Excel creates lock files (`.~lock.*` or `~$*`) when opening Excel files. If Excel crashes or doesn't close properly, these lock files remain and prevent the file from being opened for editing.

---

## Solution 1: Update Command to Remove Old Files (Recommended)

Replace your current command with this version that removes old files and lock files first:

```bash
cd /home/bernd/docker-container/balancesheet && \
rm -f /mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx \
      /mnt/nvme/raid/bernd/steuererklaerungen/.~lock.hollgasse_1_54_$(date +\%Y).xlsx# \
      /mnt/nvme/raid/bernd/steuererklaerungen/~$hollgasse_1_54_$(date +\%Y).xlsx && \
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54 >> \
/home/bernd/docker-container/balancesheet/logs/hollgasse_1_54.log 2>&1 && \
/usr/bin/docker cp $(/usr/bin/docker-compose ps -q web):/app/hollgasse_1_54_$(date +\%Y).xlsx \
/mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx && \
chmod 664 /mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx
```

**Changes:**
1. Added `rm -f` BEFORE generating the report to delete:
   - Old Excel file
   - LibreOffice lock file (`.~lock.*`)
   - Excel lock file (`~$*`)

---

## Solution 2: Manual Cleanup

If you still get the error, manually check for and remove lock files:

### Check for lock files:
```bash
# Check for any lock files in the directory
ls -la /mnt/nvme/raid/bernd/steuererklaerungen/ | grep -E '\.~lock\.|~\$'
```

### Remove lock files:
```bash
# Remove all lock files for hollgasse_1_54
rm -f /mnt/nvme/raid/bernd/steuererklaerungen/.~lock.hollgasse_1_54*
rm -f /mnt/nvme/raid/bernd/steuererklaerungen/~$hollgasse_1_54*
```

---

## Solution 3: Check if File is Open

Check if any process has the file open:

```bash
# Check if the file is currently being accessed
lsof /mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_2025.xlsx

# Check all open Excel files
lsof | grep -i excel
```

If you see output, close Excel or kill the process:
```bash
# Find the process ID (PID) and kill it
kill -9 <PID>
```

---

## Solution 4: Network Share Issue

If `/mnt/nvme/raid/` is a network share (SMB/CIFS/NFS), you might need to:

### For SMB/CIFS shares:
```bash
# Remount the share with nobrl option (no byte-range locks)
sudo mount -o remount,nobrl /mnt/nvme/raid
```

### Check mount options:
```bash
mount | grep nvme
```

If it shows `cifs` or `smb`, add to `/etc/fstab`:
```
//server/share /mnt/nvme/raid cifs credentials=/path/to/creds,nobrl,uid=bernd,gid=bernd 0 0
```

---

## Solution 5: Create Timestamped Backup Script

Create a wrapper script that uses unique filenames:

```bash
#!/bin/bash
# File: /home/bernd/docker-container/balancesheet/generate_reports.sh

TIMESTAMP=$(date +\%Y\%m\%d_\%H\%M\%S)
YEAR=$(date +\%Y)
DEST_DIR="/mnt/nvme/raid/bernd/steuererklaerungen"

cd /home/bernd/docker-container/balancesheet

# Generate report
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54 >> \
  logs/hollgasse_1_54.log 2>&1

# Copy with timestamp
/usr/bin/docker cp $(/usr/bin/docker-compose ps -q web):/app/hollgasse_1_54_${YEAR}.xlsx \
  ${DEST_DIR}/hollgasse_1_54_${YEAR}_${TIMESTAMP}.xlsx

# Fix permissions
chmod 664 ${DEST_DIR}/hollgasse_1_54_${YEAR}_${TIMESTAMP}.xlsx

# Create/update symlink to latest file
ln -sf hollgasse_1_54_${YEAR}_${TIMESTAMP}.xlsx ${DEST_DIR}/hollgasse_1_54_${YEAR}_latest.xlsx

echo "Report created: hollgasse_1_54_${YEAR}_${TIMESTAMP}.xlsx"
```

This creates files like:
- `hollgasse_1_54_2025_20250103_143022.xlsx` (timestamped)
- `hollgasse_1_54_2025_latest.xlsx` (symlink to latest)

---

## Recommended Action Plan

1. **First, try Solution 1** - Update your command to remove old files first
2. **If still locked**, run Solution 2 manually to clean up lock files
3. **If problem persists**, check Solution 3 to see if Excel has the file open
4. **If on network share**, investigate Solution 4 for mounting issues
5. **For automation**, consider Solution 5 with timestamped files

---

## Testing

After implementing Solution 1, test:

```bash
# Run the updated command
cd /home/bernd/docker-container/balancesheet && ...

# Check the file can be opened
libreoffice --headless --convert-to pdf hollgasse_1_54_2025.xlsx

# Or just try to open it in Excel - should work without lock warning
```

---

## Prevention

To prevent future lock file issues:

1. ✅ Always close Excel properly (don't kill the process)
2. ✅ Use `rm -f` to clean up before generating new reports
3. ✅ Consider using timestamped filenames for historical tracking
4. ✅ If using network shares, use `nobrl` mount option

---

## Quick Fix Right Now

Run this immediately to unlock the file:

```bash
# Remove all lock files
rm -f /mnt/nvme/raid/bernd/steuererklaerungen/.~lock.* \
      /mnt/nvme/raid/bernd/steuererklaerungen/~$*

# Remove the old Excel file
rm -f /mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_2025.xlsx

# Now run your command again - should work
```
