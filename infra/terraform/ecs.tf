# ECS cluster, task definitions and Fargate services.
#
# desired_count is driven by var.desired_count: 0 leaves the platform switched
# off at near-zero cost, 1 brings AeroTracker up.

resource "aws_ecs_task_definition" "scheduler" {
  container_definitions = jsonencode([
    {
      "name"      = "aerotracker-scheduler"
      "image"     = "ghcr.io/escalasebastian/aerotracker/aerotracker-scheduler:latest"
      "cpu"       = 0
      "essential" = true
      "environment" = [
        {
          "name"  = "SPRING_RABBITMQ_HOST"
          "value" = "rabbitmq.aerotracker.local"
        },
        {
          "name"  = "DB_USERNAME"
          "value" = "aerotracker_admin"
        },
        {
          "name"  = "DB_URL"
          "value" = "jdbc:postgresql://${aws_db_instance.main.endpoint}/aerotracker"
        },
        {
          "name"  = "JAVA_TOOL_OPTIONS"
          "value" = "-Xms128m -Xmx192m -XX:+UseSerialGC"
        },
      ]
      "logConfiguration" = {
        "logDriver" = "awslogs"
        "options" = {
          "awslogs-group"         = "/ecs/aerotracker-scheduler"
          "awslogs-region"        = "eu-west-1"
          "awslogs-stream-prefix" = "scheduler"
        }
      }
      "secrets" = [
        {
          "name"      = "DB_PASSWORD"
          "valueFrom" = aws_ssm_parameter.db_password.arn
        },
      ]
    },
  ])
  cpu                      = "256"
  enable_fault_injection   = false
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  family                   = "aerotracker-scheduler"
  ipc_mode                 = null
  memory                   = "512"
  network_mode             = "awsvpc"
  pid_mode                 = null
  requires_compatibilities = ["FARGATE"]
  skip_destroy             = null
  tags                     = {}
  tags_all                 = {}
  task_role_arn            = null
  track_latest             = false
}

resource "aws_ecs_task_definition" "notification" {
  container_definitions = jsonencode([
    {
      "name"      = "aerotracker-notification"
      "image"     = "ghcr.io/escalasebastian/aerotracker/aerotracker-notification:latest"
      "cpu"       = 0
      "essential" = true
      "environment" = [
        {
          "name"  = "SPRING_RABBITMQ_HOST"
          "value" = "rabbitmq.aerotracker.local"
        },
        {
          "name"  = "DB_USERNAME"
          "value" = "aerotracker_admin"
        },
        {
          "name"  = "DB_URL"
          "value" = "jdbc:postgresql://${aws_db_instance.main.endpoint}/aerotracker"
        },
        {
          "name"  = "JAVA_TOOL_OPTIONS"
          "value" = "-Xms128m -Xmx192m -XX:+UseSerialGC"
        },
      ]
      "logConfiguration" = {
        "logDriver" = "awslogs"
        "options" = {
          "awslogs-group"         = "/ecs/aerotracker-notification"
          "awslogs-region"        = "eu-west-1"
          "awslogs-stream-prefix" = "notification"
        }
      }
      "secrets" = [
        {
          "name"      = "TELEGRAM_BOT_TOKEN"
          "valueFrom" = aws_ssm_parameter.telegram_bot_token.arn
        },
        {
          "name"      = "DB_PASSWORD"
          "valueFrom" = aws_ssm_parameter.db_password.arn
        },
      ]
    },
  ])
  cpu                      = "256"
  enable_fault_injection   = false
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  family                   = "aerotracker-notification"
  ipc_mode                 = null
  memory                   = "512"
  network_mode             = "awsvpc"
  pid_mode                 = null
  requires_compatibilities = ["FARGATE"]
  skip_destroy             = null
  tags                     = {}
  tags_all                 = {}
  task_role_arn            = null
  track_latest             = false
}

resource "aws_ecs_cluster" "main" {
  name     = "aerotracker-cluster"
  tags     = {}
  tags_all = {}
  setting {
    name  = "containerInsights"
    value = "disabled"
  }
}

resource "aws_ecs_task_definition" "rabbitmq" {
  container_definitions = jsonencode([
    {
      "name"  = "rabbitmq"
      "image" = "rabbitmq:3-management-alpine"
      "cpu"   = 0
      "portMappings" = [
        {
          "containerPort" = 5672
          "hostPort"      = 5672
          "protocol"      = "tcp"
          "name"          = "rabbitmq-amqp"
        },
        {
          "containerPort" = 15672
          "hostPort"      = 15672
          "protocol"      = "tcp"
          "name"          = "rabbitmq-mgmt"
        },
      ]
      "essential" = true
      "logConfiguration" = {
        "logDriver" = "awslogs"
        "options" = {
          "awslogs-group"         = "/ecs/aerotracker-rabbitmq"
          "awslogs-region"        = "eu-west-1"
          "awslogs-stream-prefix" = "rabbitmq"
        }
      }
    },
  ])
  cpu                      = "256"
  enable_fault_injection   = false
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  family                   = "rabbitmq"
  ipc_mode                 = null
  memory                   = "512"
  network_mode             = "awsvpc"
  pid_mode                 = null
  requires_compatibilities = ["FARGATE"]
  skip_destroy             = null
  tags                     = {}
  tags_all                 = {}
  task_role_arn            = null
  track_latest             = false
}

resource "aws_ecs_service" "price_checker" {
  availability_zone_rebalancing      = "ENABLED"
  cluster                            = aws_ecs_cluster.main.arn
  deployment_maximum_percent         = 200
  deployment_minimum_healthy_percent = 100
  desired_count                      = var.desired_count
  enable_ecs_managed_tags            = false
  enable_execute_command             = false
  force_delete                       = null
  force_new_deployment               = null
  health_check_grace_period_seconds  = 0
  iam_role                           = "/aws-service-role/ecs.amazonaws.com/AWSServiceRoleForECS"
  launch_type                        = "FARGATE"
  name                               = "aerotracker-price-checker"
  platform_version                   = "LATEST"
  propagate_tags                     = "NONE"
  scheduling_strategy                = "REPLICA"
  tags                               = {}
  tags_all                           = {}
  task_definition                    = aws_ecs_task_definition.price_checker.arn
  triggers                           = {}
  wait_for_steady_state              = null
  deployment_circuit_breaker {
    enable   = false
    rollback = false
  }
  deployment_controller {
    type = "ECS"
  }
  network_configuration {
    assign_public_ip = true
    security_groups  = [aws_security_group.ecs.id]
    subnets          = [aws_subnet.public_1b.id, aws_subnet.public_1a.id]
  }
}

resource "aws_ecs_service" "notification" {
  availability_zone_rebalancing      = "ENABLED"
  cluster                            = aws_ecs_cluster.main.arn
  deployment_maximum_percent         = 200
  deployment_minimum_healthy_percent = 100
  desired_count                      = var.desired_count
  enable_ecs_managed_tags            = false
  enable_execute_command             = false
  force_delete                       = null
  force_new_deployment               = null
  health_check_grace_period_seconds  = 0
  iam_role                           = "/aws-service-role/ecs.amazonaws.com/AWSServiceRoleForECS"
  launch_type                        = "FARGATE"
  name                               = "aerotracker-notification"
  platform_version                   = "LATEST"
  propagate_tags                     = "NONE"
  scheduling_strategy                = "REPLICA"
  tags                               = {}
  tags_all                           = {}
  task_definition                    = aws_ecs_task_definition.notification.arn
  triggers                           = {}
  wait_for_steady_state              = null
  deployment_circuit_breaker {
    enable   = false
    rollback = false
  }
  deployment_controller {
    type = "ECS"
  }
  network_configuration {
    assign_public_ip = true
    security_groups  = [aws_security_group.ecs.id]
    subnets          = [aws_subnet.public_1b.id, aws_subnet.public_1a.id]
  }
}

resource "aws_ecs_task_definition" "api" {
  container_definitions = jsonencode([
    {
      "name"  = "aerotracker-api"
      "image" = "ghcr.io/escalasebastian/aerotracker/aerotracker-api:latest"
      "cpu"   = 0
      "portMappings" = [
        {
          "containerPort" = 8080
          "hostPort"      = 8080
          "protocol"      = "tcp"
          "name"          = "api"
        },
      ]
      "essential" = true
      "environment" = [
        {
          "name"  = "SPRING_RABBITMQ_HOST"
          "value" = "rabbitmq.aerotracker.local"
        },
        {
          "name"  = "DB_USERNAME"
          "value" = "aerotracker_admin"
        },
        {
          "name"  = "DB_URL"
          "value" = "jdbc:postgresql://${aws_db_instance.main.endpoint}/aerotracker"
        },
        {
          "name"  = "JAVA_TOOL_OPTIONS"
          "value" = "-Xms128m -Xmx192m -XX:+UseSerialGC"
        },
      ]
      "logConfiguration" = {
        "logDriver" = "awslogs"
        "options" = {
          "awslogs-group"         = "/ecs/aerotracker-api"
          "awslogs-region"        = "eu-west-1"
          "awslogs-stream-prefix" = "api"
        }
      }
      "secrets" = [
        {
          "name"      = "TELEGRAM_BOT_TOKEN"
          "valueFrom" = aws_ssm_parameter.telegram_bot_token.arn
        },
        {
          "name"      = "DB_PASSWORD"
          "valueFrom" = aws_ssm_parameter.db_password.arn
        },
      ]
    },
  ])
  cpu                      = "256"
  enable_fault_injection   = false
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  family                   = "aerotracker-api"
  ipc_mode                 = null
  memory                   = "512"
  network_mode             = "awsvpc"
  pid_mode                 = null
  requires_compatibilities = ["FARGATE"]
  skip_destroy             = null
  tags                     = {}
  tags_all                 = {}
  task_role_arn            = null
  track_latest             = false
}

resource "aws_ecs_task_definition" "price_checker" {
  container_definitions = jsonencode([
    {
      "name"      = "aerotracker-price-checker"
      "image"     = "ghcr.io/escalasebastian/aerotracker/aerotracker-price-checker:latest"
      "cpu"       = 0
      "essential" = true
      "environment" = [
        {
          "name"  = "SPRING_RABBITMQ_HOST"
          "value" = "rabbitmq.aerotracker.local"
        },
        {
          "name"  = "DB_USERNAME"
          "value" = "aerotracker_admin"
        },
        {
          "name"  = "DB_URL"
          "value" = "jdbc:postgresql://${aws_db_instance.main.endpoint}/aerotracker"
        },
        {
          "name"  = "JAVA_TOOL_OPTIONS"
          "value" = "-Xms128m -Xmx192m -XX:+UseSerialGC"
        },
      ]
      "logConfiguration" = {
        "logDriver" = "awslogs"
        "options" = {
          "awslogs-group"         = "/ecs/aerotracker-price-checker"
          "awslogs-region"        = "eu-west-1"
          "awslogs-stream-prefix" = "price-checker"
        }
      }
      "secrets" = [
        {
          "name"      = "DB_PASSWORD"
          "valueFrom" = aws_ssm_parameter.db_password.arn
        },
        {
          "name"      = "SERPAPI_KEY"
          "valueFrom" = aws_ssm_parameter.serpapi_key.arn
        },
      ]
    },
  ])
  cpu                      = "256"
  enable_fault_injection   = false
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  family                   = "aerotracker-price-checker"
  ipc_mode                 = null
  memory                   = "512"
  network_mode             = "awsvpc"
  pid_mode                 = null
  requires_compatibilities = ["FARGATE"]
  skip_destroy             = null
  tags                     = {}
  tags_all                 = {}
  task_role_arn            = null
  track_latest             = false
}

resource "aws_ecs_service" "scheduler" {
  availability_zone_rebalancing      = "ENABLED"
  cluster                            = aws_ecs_cluster.main.arn
  deployment_maximum_percent         = 200
  deployment_minimum_healthy_percent = 100
  desired_count                      = var.desired_count
  enable_ecs_managed_tags            = false
  enable_execute_command             = false
  force_delete                       = null
  force_new_deployment               = null
  health_check_grace_period_seconds  = 0
  iam_role                           = "/aws-service-role/ecs.amazonaws.com/AWSServiceRoleForECS"
  launch_type                        = "FARGATE"
  name                               = "aerotracker-scheduler"
  platform_version                   = "LATEST"
  propagate_tags                     = "NONE"
  scheduling_strategy                = "REPLICA"
  tags                               = {}
  tags_all                           = {}
  task_definition                    = aws_ecs_task_definition.scheduler.arn
  triggers                           = {}
  wait_for_steady_state              = null
  deployment_circuit_breaker {
    enable   = false
    rollback = false
  }
  deployment_controller {
    type = "ECS"
  }
  network_configuration {
    assign_public_ip = true
    security_groups  = [aws_security_group.ecs.id]
    subnets          = [aws_subnet.public_1b.id, aws_subnet.public_1a.id]
  }
}

resource "aws_ecs_service" "rabbitmq" {
  availability_zone_rebalancing      = "ENABLED"
  cluster                            = aws_ecs_cluster.main.arn
  deployment_maximum_percent         = 200
  deployment_minimum_healthy_percent = 100
  desired_count                      = var.desired_count
  enable_ecs_managed_tags            = false
  enable_execute_command             = false
  force_delete                       = null
  force_new_deployment               = null
  health_check_grace_period_seconds  = 0
  iam_role                           = "/aws-service-role/ecs.amazonaws.com/AWSServiceRoleForECS"
  launch_type                        = "FARGATE"
  name                               = "rabbitmq"
  platform_version                   = "LATEST"
  propagate_tags                     = "NONE"
  scheduling_strategy                = "REPLICA"
  tags                               = {}
  tags_all                           = {}
  task_definition                    = aws_ecs_task_definition.rabbitmq.arn
  triggers                           = {}
  wait_for_steady_state              = null
  deployment_circuit_breaker {
    enable   = false
    rollback = false
  }
  deployment_controller {
    type = "ECS"
  }
  network_configuration {
    assign_public_ip = true
    security_groups  = [aws_security_group.ecs.id]
    subnets          = [aws_subnet.public_1b.id, aws_subnet.public_1a.id]
  }
  service_registries {
    container_name = null
    container_port = 0
    port           = 0
    registry_arn   = aws_service_discovery_service.rabbitmq.arn
  }
}

resource "aws_ecs_service" "api" {
  availability_zone_rebalancing      = "ENABLED"
  cluster                            = aws_ecs_cluster.main.arn
  deployment_maximum_percent         = 200
  deployment_minimum_healthy_percent = 100
  desired_count                      = var.desired_count
  enable_ecs_managed_tags            = false
  enable_execute_command             = false
  force_delete                       = null
  force_new_deployment               = null
  health_check_grace_period_seconds  = 0
  iam_role                           = "/aws-service-role/ecs.amazonaws.com/AWSServiceRoleForECS"
  launch_type                        = "FARGATE"
  name                               = "aerotracker-api"
  platform_version                   = "LATEST"
  propagate_tags                     = "NONE"
  scheduling_strategy                = "REPLICA"
  tags                               = {}
  tags_all                           = {}
  task_definition                    = aws_ecs_task_definition.api.arn
  triggers                           = {}
  wait_for_steady_state              = null
  deployment_circuit_breaker {
    enable   = false
    rollback = false
  }
  deployment_controller {
    type = "ECS"
  }
  network_configuration {
    assign_public_ip = true
    security_groups  = [aws_security_group.ecs.id]
    subnets          = [aws_subnet.public_1b.id, aws_subnet.public_1a.id]
  }
}
