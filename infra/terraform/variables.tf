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

variable "desired_count" {
  description = "Number of tasks per ECS service. 0 keeps the whole platform switched off at near-zero cost; set to 1 to bring AeroTracker up for a demo."
  type        = number
  default     = 0
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

variable "db_snapshot_identifier" {
  description = "Final snapshot the RDS instance is restored from. Set to null to create an empty database instead."
  type        = string
  default     = "aerotracker-db-final-2026-08-26"
}

variable "ssh_allowed_cidr" {
  description = "Single address allowed to SSH into the bastion, e.g. \"203.0.113.10/32\". Null keeps SSH closed, which is the default."
  type        = string
  default     = null
}

variable "ssm_parameter_prefix" {
  description = "Prefix under which the application secrets are stored in SSM Parameter Store."
  type        = string
  default     = "/aerotracker"
}
