# infra/aws — Deprecated

These PowerShell scripts were used to hand-provision the Phase 7 infrastructure (VPC, RDS, EC2, IAM, CloudWatch log groups) directly through the AWS CLI, before the project adopted Infrastructure as Code.

**They are kept only as a historical record of how Phase 7 was originally built. Do not run them again.**

As of Phase 8, the same infrastructure — plus the ECS cluster, task definitions, services, Cloud Map namespace, dashboard and alarms that were later added by hand through the console — is described and managed in `infra/terraform/`. Re-running these scripts would create resources Terraform does not know about, or drift the ones it already manages. See `docs/aws-setup.md` for the current provisioning and operating workflow.
