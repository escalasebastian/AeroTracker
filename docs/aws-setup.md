# AWS Deployment Guide — AeroTracker

This guide documents how to provision, operate and tear down the AeroTracker cloud infrastructure in region **`eu-west-1` (Ireland)** using **Terraform** (`infra/terraform/`).

The account runs under AWS's credit-based Free Tier, not the legacy 12-month tier: Fargate and RDS are **not** free by default. The whole platform is controlled by a single boolean, `platform_enabled`, which defaults to `false`: no ECS task runs, and neither the RDS instance nor the Cloud Map namespace exists, so the residual cost stays near $0/month. It is brought up on demand for demos.

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

Secrets (`db_password`, `telegram_bot_token`, `serpapi_key`) and the alert email address are supplied through a gitignored `secrets.auto.tfvars` file, never committed:

```hcl
db_password        = "..."
telegram_bot_token = "..."
serpapi_key        = "..."
alert_email        = "..."
```

After the first `terraform apply`, AWS sends a confirmation email to `alert_email`. Click its link once; alerts are not delivered until the subscription is confirmed.

Review the plan before applying anything:

```bash
terraform plan
```

---

## 3. Bringing the platform up

The platform stays switched off by default (`platform_enabled = false` in `terraform.tfvars`). To bring it up for a demo:

```bash
terraform apply -var="platform_enabled=true"
```

Terraform then:

1. Creates an empty RDS instance. This is the slowest step and can take 5 to 15 minutes.
2. Creates the Cloud Map namespace `aerotracker.local`, through which the services reach RabbitMQ at `rabbitmq.aerotracker.local`.
3. Registers new task definitions whose `DB_URL` points at the new database endpoint.
4. Starts one task for each of the 5 ECS services (api, scheduler, price-checker, notification, rabbitmq).

Because the task definitions reference the RDS endpoint, Terraform only starts the services once the database exists. Wait a minute for the tasks to reach `RUNNING`, then verify:

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
terraform apply
```

With `platform_enabled` back at its default `false`, Terraform scales every ECS service to zero, deletes the RDS instance without keeping a snapshot, and deletes the Cloud Map namespace. The namespace is backed by a Route 53 private hosted zone, billed 0.50 USD for each month in which it exists for more than 12 hours, so months with the platform off cost nothing. Confirm nothing is left running:

```bash
aws ecs list-tasks --cluster aerotracker-cluster --region eu-west-1
aws rds describe-db-instances --region eu-west-1 --query 'DBInstances[].DBInstanceIdentifier'
```

Two empty results mean the platform is back to its near-zero-cost idle state.

---

## 5. Database lifecycle

The database is disposable by design. Every start-up creates an empty RDS instance with the `aerotracker` database, and the api service's Flyway migrations rebuild the schema on boot. The scheduler and price-checker only validate the schema, so if one of them starts before the api has migrated it fails once and ECS restarts it; that is expected on the first minute of a fresh start-up. Every shutdown deletes the instance with no final snapshot and no automated backups, so nothing related to RDS is left to bill.

The trade-off is that users and subscriptions do not survive a shutdown. That is acceptable because the platform only runs for demos; keeping the data would mean paying for snapshot storage every month it sits unused.

The instance takes its master password from `db_password` in `secrets.auto.tfvars`, the same variable that feeds the `DB_PASSWORD` SSM parameter, so changing it there before a start-up rotates the password for both.

---

## 6. Alerts

While the platform is enabled, each of the 5 ECS services has a CloudWatch alarm named `AeroTracker-<service>-Down` that emails `alert_email` through the `aerotracker-alerts` SNS topic when the service stops running.

The alarms rely on the free per-minute CPU metric that ECS publishes for every running service. The alarm condition can never be true, so the alarm stays `OK` while datapoints arrive and moves to `INSUFFICIENT_DATA` after 5 minutes without any, which is what happens when the task is gone. Only that transition sends an email, with the subject `INSUFFICIENT_DATA: "AeroTracker-<service>-Down"`. Start-ups stay silent because a new alarm begins in `INSUFFICIENT_DATA` as its initial state, not as a transition, and the brief restart of the scheduler or price-checker on a fresh database is shorter than the 5-minute window.

The alarms are removed when the platform is switched off. The SNS topic and its subscription stay, so the email address only has to be confirmed once. The 5 alarms fit in the CloudWatch free allowance of 10 alarms, and SNS email delivery is free up to 1,000 emails per month.

---

## 7. Accessing the database directly

The database has no public access, and the bastion host only exists while SSH access is requested. Setting `ssh_allowed_cidr` creates the bastion and opens SSH to that single address. The platform must be enabled as well, since the database only exists then:

```bash
terraform apply -var="platform_enabled=true" -var="ssh_allowed_cidr=<your-public-ip>/32"
terraform output
ssh -i infra/aws/aerotracker-key.pem -L 5432:<rds-endpoint>:5432 ubuntu@<bastion-public-ip>
```

`terraform output` shows `rds_endpoint` and `bastion_public_ip`.

When done, run `terraform apply -var="platform_enabled=true"` to keep the platform up, or a plain `terraform apply` to switch everything off. Both leave `ssh_allowed_cidr` unset, which destroys the bastion and closes SSH. While it exists, the bastion (a t3.micro with a public IPv4 address and a 20 GB disk) costs about 0.45 USD per day.

---

## 8. History: how Phase 7 was originally provisioned

Phases 7.1–7.4 were first built by hand with the PowerShell scripts under `infra/aws/` (VPC/RDS, EC2, IAM/CloudWatch), documented for the historical record but no longer the source of truth — see `infra/aws/README.md`. Phase 8 adopted that live infrastructure into Terraform via `import` blocks; **the scripts should not be run again**, since Terraform now owns the state of these resources.
