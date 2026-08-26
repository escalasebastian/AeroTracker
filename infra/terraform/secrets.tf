# Application secrets, stored as SecureString parameters in SSM Parameter Store.
#
# ECS task definitions reference these through "secrets"/"valueFrom" instead of
# carrying the values as plain environment variables, so the credentials are no
# longer readable by anyone holding ecs:DescribeTaskDefinition.
#
# Standard SSM parameters are free of charge, which keeps the platform within
# its zero-cost constraint.

resource "aws_ssm_parameter" "db_password" {
  name        = "${var.ssm_parameter_prefix}/db_password"
  description = "Master password of the AeroTracker RDS PostgreSQL instance."
  type        = "SecureString"
  value       = var.db_password
}

resource "aws_ssm_parameter" "telegram_bot_token" {
  name        = "${var.ssm_parameter_prefix}/telegram_bot_token"
  description = "Telegram bot token used by the api and notification services."
  type        = "SecureString"
  value       = var.telegram_bot_token
}
