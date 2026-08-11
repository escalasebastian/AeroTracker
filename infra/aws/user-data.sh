#!/bin/bash
# ==============================================================================
# AeroTracker EC2 User-Data Bootstrap Script
# ==============================================================================
# This script provisions:
# 1. 2GB Swap space to safeguard system memory under load
# 2. Latest Docker Engine & Docker Compose Plugin (Official Docker Repo)
# 3. User permissions for 'ubuntu' user to run Docker
# ==============================================================================

set -euo pipefail

export DEBIAN_FRONTEND=noninteractive

echo "[INFO] Starting AeroTracker EC2 provisioning..."

# ------------------------------------------------------------------------------
# 1. Configure 2GB Swap Space
# ------------------------------------------------------------------------------
echo "[INFO] Setting up 2GB Swap space..."
if [ ! -f /swapfile ]; then
    fallocate -l 2G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
    # Lower swappiness to favor physical RAM until necessary
    sysctl vm.swappiness=20
    echo 'vm.swappiness=20' >> /etc/sysctl.conf
fi
echo "[SUCCESS] Swap space configured successfully."

# ------------------------------------------------------------------------------
# 2. Update & Install Dependencies
# ------------------------------------------------------------------------------
echo "[INFO] Updating package repositories..."
apt-get update -y
apt-get install -y ca-certificates curl gnupg lsb-release

# ------------------------------------------------------------------------------
# 3. Install Docker Engine & Docker Compose
# ------------------------------------------------------------------------------
echo "[INFO] Installing official Docker Engine & Docker Compose..."
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# ------------------------------------------------------------------------------
# 4. User Group & Service Configuration
# ------------------------------------------------------------------------------
echo "[INFO] Configuring Docker permissions..."
usermod -aG docker ubuntu
systemctl enable docker
systemctl start docker

# Create application directory
mkdir -p /home/ubuntu/aerotracker
chown -R ubuntu:ubuntu /home/ubuntu/aerotracker

echo "[SUCCESS] EC2 bootstrap finished successfully."
