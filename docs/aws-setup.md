# AWS Deployment Guide — AeroTracker

This guide documents the procedures required to provision and manage the cloud infrastructure for **AeroTracker** on AWS within the **Free Tier** in region **`eu-west-1` (Ireland)**.

---

## 1. Prerequisites

1. Active AWS Account with a configured **Zero-Spend Budget** ($0.01 limit).
2. **AWS CLI v2** installed and configured locally (`aws configure`).
3. Telegram Bot Token from `@BotFather`.

---

## 2. Phase 7.1 — Networking & Database Setup

Execute the Phase 7.1 provisioning script to create the VPC, subnets, security groups, and RDS PostgreSQL 16 instance:

```powershell
.\infra\aws\setup-infrastructure.ps1 -MasterUserPassword "YourSecurePassword123!"
```

To verify that RDS has transitioned to `available`:
```powershell
aws rds describe-db-instances --db-instance-identifier aerotracker-db --region eu-west-1 --query 'DBInstances[0].[DBInstanceStatus,Endpoint.Address]'
```

---

## 3. Phase 7.2 — EC2 Compute Deployment

### Step 1: Provision EC2 Instance

Run the provisioning script to create the SSH key pair and launch the Ubuntu 24.04 `t3.micro` instance:

```powershell
.\infra\aws\04-ec2-instance.ps1
```

The script will output the instance's **Public IP** and SSH connection command:
```bash
ssh -i infra/aws/aerotracker-key.pem ubuntu@<EC2_PUBLIC_IP>
```

---

### Step 2: Deploy Containers to EC2

1. **Prepare Environment File (`.env` on EC2):**
   Create `/home/ubuntu/aerotracker/.env` on the EC2 server with:

   ```env
   DB_HOST=aerotracker-db.c9i66uq80gy3.eu-west-1.rds.amazonaws.com
   DB_PORT=5432
   DB_NAME=aerotracker
   DB_USERNAME=aerotracker_admin
   DB_PASSWORD=YourSecurePassword123!
   TELEGRAM_BOT_TOKEN=your_telegram_bot_token
   ```

2. **Copy Production Compose File:**
   Copy `docker-compose.prod.yml` to the EC2 server:
   ```bash
   scp -i infra/aws/aerotracker-key.pem docker-compose.prod.yml ubuntu@<EC2_PUBLIC_IP>:/home/ubuntu/aerotracker/
   ```

3. **Start the Microservices:**
   SSH into the instance and run:
   ```bash
   cd /home/ubuntu/aerotracker
   docker compose -f docker-compose.prod.yml up -d
   ```

---

## 4. Verification & Testing

1. **Check Container Status:**
   ```bash
   docker compose -f docker-compose.prod.yml ps
   ```
2. **Inspect Logs:**
   ```bash
   docker compose -f docker-compose.prod.yml logs -f aerotracker-api
   ```
3. **Telegram Verification:**
   - Send `/start` to your Telegram Bot.
   - Send `/price MAD AMS 2026-08-10` to query live flight prices.
   - Send `/track MAD AMS 2026-08-10 150` to save a persistent subscription into RDS.

---

## 5. Decommissioning & Cost Control

To stop or delete resources and avoid any charges:

```powershell
# Stop EC2 instance when not in use
aws ec2 stop-instances --instance-ids <INSTANCE_ID> --region eu-west-1

# Terminate EC2 instance
aws ec2 terminate-instances --instance-ids <INSTANCE_ID> --region eu-west-1

# Delete RDS instance (if no longer needed)
aws rds delete-db-instance --db-instance-identifier aerotracker-db --skip-final-snapshot --delete-automated-backups --region eu-west-1
```
