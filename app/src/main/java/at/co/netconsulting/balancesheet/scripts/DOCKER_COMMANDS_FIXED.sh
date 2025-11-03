#!/bin/bash

# Hollgasse 1 Report (Taxable Entries)
cd /home/bernd/docker-container/balancesheet && \
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54 >> \
/home/bernd/docker-container/balancesheet/logs/hollgasse_1_54.log 2>&1 && \
/usr/bin/docker cp $(/usr/bin/docker-compose ps -q web):/app/hollgasse_1_54_$(date +\%Y).xlsx \
/mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx && \
chmod 664 /mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx

# Hollgasse 54 Report (Non-Taxable Entries)
cd /home/bernd/docker-container/balancesheet && \
/usr/bin/docker-compose exec -T web python3 hollgasse.py Hollgasse_1_54_nontaxable >> \
/home/bernd/docker-container/balancesheet/logs/hollgasse_1_54_nontaxable.log 2>&1 && \
/usr/bin/docker cp $(/usr/bin/docker-compose ps -q web):/app/hollgasse_1_54_nontaxable_$(date +\%Y).xlsx \
/mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_nontaxable_$(date +\%Y).xlsx && \
chmod 664 /mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_nontaxable_$(date +\%Y).xlsx

# Alternative: Change ownership to your user
# Replace 'bernd:bernd' with your username:group
# chown bernd:bernd /mnt/nvme/raid/bernd/steuererklaerungen/hollgasse_1_54_$(date +\%Y).xlsx
