# AWS Deployment Guide — AeroTracker

This guide documents how to provision, operate and tear down the AeroTracker cloud infrastructure in region **`eu-west-1` (Ireland)** using **Terraform** (`infra/terraform/`).

The account runs under AWS's credit-based Free Tier, not the legacy 12-month tier: Fargate and RDS are **not** free by default. The infrastructure defaults to `desired_count = 0` (switched off) to keep the residual cost near $0/month, and is brought up on demand for demos.

---

## 1. Prerequisites

1. Active AWS Account with a **Zero-Spend Budget** alert configured.
2. **AWS CLI v2** installed and configured locally (`aws configure`).
3. **Terraform >= 1.5** installed (`winget install Hashicorp.Terraform` on Windows).
4. Telegram Bot Token from `@BotFather`.

---

## 2. First-time setup

```bash
cd infra/terraform
terraform init
```

Secrets (`db_password`, `telegram_bot_token`) are supplied through a gitignored `secrets.auto.tfvars` file, never committed:

```hcl
db_password        = "..."
telegram_bot_token = "..."
```

Review the plan before applying anything:

```bash
terraform plan
```

---

## 3. Bringing the platform up

The platform stays at zero cost by default (`desired_count = 0` in `terraform.tfvars`). To bring every ECS service up for a demo:

```bash
terraform apply -var="desired_count=1"
```

This starts the 5 Fargate tasks (api, scheduler, price-checker, notification, rabbitmq) against the existing RDS instance and Cloud Map namespace. Wait a minute for the tasks to reach `RUNNING`, then verify:

```bash
aws ecs describe-services --cluster aerotracker-cluster --region eu-west-1 \
  --services aerotracker-api aerotracker-scheduler aerotracker-price-checker aerotracker-notification rabbitmq \
  --query 'services[].{Name:serviceName,Running:runningCount}' --output table
```

**Telegram verification:**
- Send `/start` to the bot.
- Send `/track MAD AMS 2027-03-15 150` to save a persistent subscription into RDS.
- Send `/list` to confirm it was stored.

Inspect logs if something doesn't come up:

```bash
aws logs tail /ecs/aerotracker-api --since 5m --region eu-west-1
```

---

## 4. Bringing the platform back down

```bash
terraform apply -var="desired_count=0"
```

or simply `terraform apply` once `terraform.tfvars` is back to its default. Confirm nothing is left running:

```bash
aws ecs list-tasks --cluster aerotracker-cluster --region eu-west-1
```

An empty result means the platform is back to its near-zero-cost idle state.

---

## 5. Restoring the database

If the RDS instance ever needs to be recreated from a snapshot (as happened when the Phase 7 infrastructure was adopted into Terraform), point `db_snapshot_identifier` at the snapshot to restore from and apply:

```bash
terraform apply -var="db_snapshot_identifier=aerotracker-db-final-2026-08-26"
```

Terraform recreates the RDS instance from that snapshot and every service that references `aws_db_instance.main.endpoint` in its `DB_URL` picks up the new endpoint automatically on its next deploy.

---

## 6. Accessing the database directly

The database has no public access. To open an SSH tunnel through the bastion host (kept stopped by default):

```bash
aws ec2 start-instances --instance-ids i-07148aa263ecf53c5 --region eu-west-1
ssh -i infra/aws/aerotracker-key.pem -L 5432:<rds-endpoint>:5432 ubuntu@<bastion-public-ip>
```

Stop the bastion again once done — its EBS volume is the only part of it that costs anything while stopped, but the instance itself would otherwise stay billable if left running.

---

## 7. History: how Phase 7 was originally provisioned

Phases 7.1–7.4 were first built by hand with the PowerShell scripts under `infra/aws/` (VPC/RDS, EC2, IAM/CloudWatch), documented for the historical record but no longer the source of truth — see `infra/aws/README.md`. Phase 8 adopted that live infrastructure into Terraform via `import` blocks; **the scripts should not be run again**, since Terraform now owns the state of these resources.
