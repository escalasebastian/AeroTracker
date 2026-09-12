variable "aws_region" {
  description = "AWS region hosting the AeroTracker infrastructure."
  type        = string
  default     = "eu-west-1"
}

variable "project_name" {
  description = "Prefix used to name and tag every resource."
  type        = string
  default     = "aerotracker"
}

variable "vpc_cidr" {
  description = "CIDR block of the AeroTracker VPC."
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnets" {
  description = "Public subnets hosting the ECS Fargate tasks, keyed by availability zone."
  type        = map(string)
  default = {
    "eu-west-1a" = "10.0.1.0/24"
    "eu-west-1b" = "10.0.2.0/24"
  }
}

variable "private_subnets" {
  description = "Private subnets hosting RDS, keyed by availability zone."
  type        = map(string)
  default = {
    "eu-west-1a" = "10.0.10.0/24"
    "eu-west-1b" = "10.0.20.0/24"
  }
}

variable "platform_enabled" {
  description = "Single on/off switch for the platform. true creates an empty RDS instance and runs one task per ECS service; false (the default) deletes RDS without a snapshot and stops every task, leaving AeroTracker at near-zero cost."
  type        = bool
  default     = false
}

variable "db_username" {
  description = "Master username of the RDS PostgreSQL instance."
  type        = string
  default     = "aerotracker_admin"
}

variable "db_password" {
  description = "Master password of the RDS PostgreSQL instance. Supplied through secrets.auto.tfvars, never committed."
  type        = string
  sensitive   = true
}

variable "telegram_bot_token" {
  description = "Telegram bot token consumed by the api and notification services. Supplied through secrets.auto.tfvars, never committed."
  type        = string
  sensitive   = true
}

variable "serpapi_key" {
  description = "SerpApi key used by the price-checker for real Google Flights prices. Supplied through secrets.auto.tfvars, never committed."
  type        = string
  sensitive   = true
}

variable "alert_email" {
  description = "Email address that receives an alert when an ECS service stops running. Supplied through secrets.auto.tfvars so it stays out of the repository."
  type        = string
  sensitive   = true
}

variable "ssh_allowed_cidr" {
  description = "Single address allowed to SSH into the bastion, e.g. \"203.0.113.10/32\". Setting it also creates the bastion host; null, the default, destroys it and keeps SSH closed."
  type        = string
  default     = null
}

variable "ssm_parameter_prefix" {
  description = "Prefix under which the application secrets are stored in SSM Parameter Store."
  type        = string
  default     = "/aerotracker"
}
