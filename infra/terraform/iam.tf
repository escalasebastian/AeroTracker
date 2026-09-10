# IAM role assumed by ECS tasks when pulling images and writing logs.

resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
  role       = aws_iam_role.ecs_task_execution.name
}

resource "aws_iam_role" "ecs_task_execution" {
  assume_role_policy = jsonencode({
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
    Version = "2012-10-17"
  })
  description           = null
  force_detach_policies = false
  max_session_duration  = 3600
  name                  = "ecsTaskExecutionRole"
  path                  = "/"
  permissions_boundary  = null
  tags                  = {}
  tags_all              = {}
}

# AmazonECSTaskExecutionRolePolicy does not grant access to customer-owned SSM
# parameters, so the execution role needs this policy to inject the secrets
# declared in secrets.tf. Without it the tasks fail during startup.
resource "aws_iam_role_policy" "ecs_task_execution_secrets" {
  name = "aerotracker-ssm-secrets-access"
  role = aws_iam_role.ecs_task_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "ssm:GetParameters",
      ]
      Resource = [
        aws_ssm_parameter.db_password.arn,
        aws_ssm_parameter.telegram_bot_token.arn,
        aws_ssm_parameter.serpapi_key.arn,
      ]
    }]
  })
}
