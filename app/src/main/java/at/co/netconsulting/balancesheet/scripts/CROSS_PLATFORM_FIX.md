# Cross-Platform Excel File Lock Fix
# Linux Docker → Windows LibreOffice

## Problem
Files created in Linux Docker container and copied to Windows share cannot be opened for editing - Windows shows "file locked by another user" error.

---

## Root Cause
Windows file locking works differently than Linux. When copying files from Linux to Windows shares:
- Linux extended attributes don't translate properly
- Windows interprets file metadata as "locked"
- SMB/CIFS protocol adds complexity

---

## Solution 1: Mount Windows Share Inside Docker (Recommended)

Make the Windows share available inside the Docker container and write directly to it.

### Step 1: Update docker-compose.yml

Add the Windows share as a volume:

```yaml
services:
  web:
    volumes:
      - ./app:/app
      - /mnt/nvme/raid/bernd/steuererklaerungen:/exports  # Mount Windows share
```

### Step 2: Update Python Script to Write Directly to Share

Modify the command to specify output path:

```bash
cd /home/bernd/docker-container/balancesheet && \
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54 /exports >> \
/home/bernd/docker-container/balancesheet/logs/hollgasse_1_54.log 2>&1
```

### Step 3: Update Python Script to Accept Output Path

The script needs to accept an optional output directory parameter. This creates the file directly on the Windows share.

---

## Solution 2: Use SCP/SFTP Instead of Docker CP (More Reliable)

Instead of `docker cp`, use secure copy which handles cross-platform better:

```bash
cd /home/bernd/docker-container/balancesheet && \
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54 >> \
/home/bernd/docker-container/balancesheet/logs/hollgasse_1_54.log 2>&1 && \
/usr/bin/docker cp $(/usr/bin/docker-compose ps -q web):/app/hollgasse_1_54_$(date +\%Y).xlsx \
/tmp/hollgasse_1_54_$(date +\%Y).xlsx && \
cp --no-preserve=all /tmp/hollgasse_1_54_$(date +\%Y).xlsx \
/mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx && \
rm -f /tmp/hollgasse_1_54_$(date +\%Y).xlsx
```

**Key:** `cp --no-preserve=all` strips all Linux metadata/attributes.

---

## Solution 3: Strip Extended Attributes (Quick Fix)

Use `attr` to remove extended attributes before copying:

```bash
cd /home/bernd/docker-container/balancesheet && \
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54 >> \
/home/bernd/docker-container/balancesheet/logs/hollgasse_1_54.log 2>&1 && \
/usr/bin/docker cp $(/usr/bin/docker-compose ps -q web):/app/hollgasse_1_54_$(date +\%Y).xlsx \
/tmp/temp_$(date +\%s).xlsx && \
setfattr -h -x user.* /tmp/temp_$(date +\%s).xlsx 2>/dev/null || true && \
cp /tmp/temp_$(date +\%s).xlsx /mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx && \
rm -f /tmp/temp_$(date +\%s).xlsx
```

---

## Solution 4: Create Temporary Copy with No Attributes (Simplest)

```bash
cd /home/bernd/docker-container/balancesheet && \
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54 >> \
/home/bernd/docker-container/balancesheet/logs/hollgasse_1_54.log 2>&1 && \
/usr/bin/docker cp $(/usr/bin/docker-compose ps -q web):/app/hollgasse_1_54_$(date +\%Y).xlsx - | \
cat > /mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx
```

This pipes through `cat` which strips metadata.

---

## Solution 5: Verify SMB Mount Options

Check if your Windows share is mounted with the right options:

```bash
# Check current mount
mount | grep "/mnt/nvme/raid"
```

**If it shows `cifs` or `smb`, remount with better options:**

```bash
# Unmount
sudo umount /mnt/nvme/raid

# Remount with nobrl (no byte-range locking)
sudo mount -t cifs //windows-server/share /mnt/nvme/raid \
  -o username=bernd,nobrl,nounix,file_mode=0664,dir_mode=0775
```

**Make it permanent in `/etc/fstab`:**
```
//windows-server/share /mnt/nvme/raid cifs credentials=/etc/samba/credentials,nobrl,nounix,file_mode=0664,dir_mode=0775,uid=bernd,gid=bernd 0 0
```

---

## Recommended Solution (Try in Order)

### 1. First, try Solution 4 (Simplest)

Replace your command with:

```bash
cd /home/bernd/docker-container/balancesheet && \
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54 >> \
/home/bernd/docker-container/balancesheet/logs/hollgasse_1_54.log 2>&1 && \
/usr/bin/docker cp $(/usr/bin/docker-compose ps -q web):/app/hollgasse_1_54_$(date +\%Y).xlsx - | \
cat > /mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx
```

**This pipes the file through `cat` which:**
- Strips Linux metadata
- Creates a "fresh" file on Windows share
- Avoids file locking issues

---

### 2. If that doesn't work, try Solution 2

```bash
cd /home/bernd/docker-container/balancesheet && \
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54 >> \
/home/bernd/docker-container/balancesheet/logs/hollgasse_1_54.log 2>&1 && \
/usr/bin/docker cp $(/usr/bin/docker-compose ps -q web):/app/hollgasse_1_54_$(date +\%Y).xlsx \
/tmp/hollgasse_temp_$(date +\%s).xlsx && \
cp --no-preserve=all /tmp/hollgasse_temp_$(date +\%s).xlsx \
/mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx && \
rm -f /tmp/hollgasse_temp_$(date +\%s).xlsx
```

**This uses `cp --no-preserve=all` which:**
- Strips all Linux permissions, ownership, timestamps
- Creates a clean file on the Windows share

---

### 3. If still fails, check SMB mount (Solution 5)

```bash
# Check how the share is mounted
mount | grep nvme

# Look for: cifs, smb, nfs
# If it's cifs/smb, you need to add 'nobrl' option
```

---

## Testing

After applying the fix:

```bash
# 1. Generate the file
[run your updated command]

# 2. Check file on Linux side
ls -l /mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_2025.xlsx

# 3. Check for extended attributes
getfattr -d /mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_2025.xlsx

# 4. Try to open on Windows
# Should open without lock warning
```

---

## Why This Happens

**Linux → Windows File Transfer Issues:**
1. Linux uses POSIX file permissions (owner, group, others)
2. Windows uses ACLs (Access Control Lists)
3. Linux extended attributes don't exist in Windows
4. CIFS/SMB protocol tries to "translate" but often fails
5. Result: Windows thinks file is locked

**The Fix:**
- Strip all Linux metadata before copying to Windows share
- Use piping (`|`) or `--no-preserve=all` flag
- Or mount the Windows share inside Docker to avoid copying altogether

---

## Quick Test - Try This First

```bash
cd /home/bernd/docker-container/balancesheet && \
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54 >> \
logs/hollgasse_1_54.log 2>&1 && \
/usr/bin/docker cp $(/usr/bin/docker-compose ps -q web):/app/hollgasse_1_54_$(date +\%Y).xlsx - | \
cat > /mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx

echo "File created - try opening on Windows now"
```

This should work immediately! 🎯
