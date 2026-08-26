# ==============================================================================
# Adoption of the existing AeroTracker infrastructure into Terraform.
#
# These blocks let Terraform take ownership of resources that were originally
# provisioned with the PowerShell scripts in infra/aws/ and, for ECS and
# CloudWatch, by hand through the AWS console. Nothing is created or destroyed
# here: the goal is a "terraform plan" reporting no changes.
#
# Delete this file once every resource has been adopted and committed.
# ==============================================================================

# ---------------------------------------------------------------------------
# Networking
# ---------------------------------------------------------------------------
import {
  to = aws_vpc.main
  id = "vpc-035f0d731c4b432e0"
}

import {
  to = aws_subnet.public_1a
  id = "subnet-04b44780e8f673532"
}

import {
  to = aws_subnet.public_1b
  id = "subnet-03fa258935823a547"
}

import {
  to = aws_subnet.private_1a
  id = "subnet-0a0367c239013c3da"
}

import {
  to = aws_subnet.private_1b
  id = "subnet-0d1a310b260390dc4"
}

import {
  to = aws_internet_gateway.main
  id = "igw-0c474b9ddf490ddeb"
}

import {
  to = aws_route_table.public
  id = "rtb-0561a1d1272d4fdcd"
}

import {
  to = aws_route_table_association.public_1a
  id = "subnet-04b44780e8f673532/rtb-0561a1d1272d4fdcd"
}

import {
  to = aws_route_table_association.public_1b
  id = "subnet-03fa258935823a547/rtb-0561a1d1272d4fdcd"
}

# ---------------------------------------------------------------------------
# Security groups
#
# Rules are adopted individually rather than inline so that Terraform does not
# try to rewrite them on the first plan.
# ---------------------------------------------------------------------------
import {
  to = aws_security_group.ecs
  id = "sg-0fba59a7dae64fb1d"
}

import {
  to = aws_security_group.rds
  id = "sg-0de3edd17d7132dbe"
}

import {
  to = aws_vpc_security_group_ingress_rule.ecs_ssh
  id = "sgr-0c3f739c6de1de603"
}

import {
  to = aws_vpc_security_group_ingress_rule.ecs_http
  id = "sgr-0aa207dd136e0b35b"
}

import {
  to = aws_vpc_security_group_ingress_rule.ecs_https
  id = "sgr-0acb5c059819687f6"
}

import {
  to = aws_vpc_security_group_ingress_rule.ecs_app
  id = "sgr-05b6942f32e6ae4de"
}

import {
  to = aws_vpc_security_group_ingress_rule.ecs_amqp
  id = "sgr-0ed5ae3d8ba2f437e"
}

import {
  to = aws_vpc_security_group_egress_rule.ecs_all
  id = "sgr-01cd79b9c07a21058"
}

import {
  to = aws_vpc_security_group_ingress_rule.rds_postgres
  id = "sgr-0cd10dfc1550ffe1c"
}

import {
  to = aws_vpc_security_group_egress_rule.rds_all
  id = "sgr-047a6b19a79fcdbf7"
}

# ---------------------------------------------------------------------------
# IAM
# ---------------------------------------------------------------------------
import {
  to = aws_iam_role.ecs_task_execution
  id = "ecsTaskExecutionRole"
}

import {
  to = aws_iam_role_policy_attachment.ecs_task_execution
  id = "ecsTaskExecutionRole/arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# ---------------------------------------------------------------------------
# Service discovery (AWS Cloud Map)
# ---------------------------------------------------------------------------
import {
  to = aws_service_discovery_private_dns_namespace.main
  id = "ns-wrxowo7ctvzie4iq:vpc-035f0d731c4b432e0"
}

import {
  to = aws_service_discovery_service.rabbitmq
  id = "srv-swe4mj4i5hrqp7ah"
}

# ---------------------------------------------------------------------------
# ECS cluster, task definitions and services
# ---------------------------------------------------------------------------
import {
  to = aws_ecs_cluster.main
  id = "aerotracker-cluster"
}

import {
  to = aws_ecs_task_definition.api
  id = "arn:aws:ecs:eu-west-1:206550328865:task-definition/aerotracker-api:1"
}

import {
  to = aws_ecs_task_definition.scheduler
  id = "arn:aws:ecs:eu-west-1:206550328865:task-definition/aerotracker-scheduler:1"
}

import {
  to = aws_ecs_task_definition.price_checker
  id = "arn:aws:ecs:eu-west-1:206550328865:task-definition/aerotracker-price-checker:1"
}

import {
  to = aws_ecs_task_definition.notification
  id = "arn:aws:ecs:eu-west-1:206550328865:task-definition/aerotracker-notification:1"
}

import {
  to = aws_ecs_task_definition.rabbitmq
  id = "arn:aws:ecs:eu-west-1:206550328865:task-definition/rabbitmq:1"
}

import {
  to = aws_ecs_service.api
  id = "aerotracker-cluster/aerotracker-api"
}

import {
  to = aws_ecs_service.scheduler
  id = "aerotracker-cluster/aerotracker-scheduler"
}

import {
  to = aws_ecs_service.price_checker
  id = "aerotracker-cluster/aerotracker-price-checker"
}

import {
  to = aws_ecs_service.notification
  id = "aerotracker-cluster/aerotracker-notification"
}

import {
  to = aws_ecs_service.rabbitmq
  id = "aerotracker-cluster/rabbitmq"
}

# ---------------------------------------------------------------------------
# Observability
# ---------------------------------------------------------------------------
import {
  to = aws_cloudwatch_log_group.api
  id = "/ecs/aerotracker-api"
}

import {
  to = aws_cloudwatch_log_group.scheduler
  id = "/ecs/aerotracker-scheduler"
}

import {
  to = aws_cloudwatch_log_group.price_checker
  id = "/ecs/aerotracker-price-checker"
}

import {
  to = aws_cloudwatch_log_group.notification
  id = "/ecs/aerotracker-notification"
}

import {
  to = aws_cloudwatch_log_group.rabbitmq
  id = "/ecs/aerotracker-rabbitmq"
}

import {
  to = aws_cloudwatch_dashboard.overview
  id = "AeroTracker-Overview"
}

import {
  to = aws_cloudwatch_metric_alarm.high_cpu
  id = "AeroTracker-High-CPU"
}

import {
  to = aws_cloudwatch_metric_alarm.high_memory
  id = "AeroTracker-High-Memory"
}

# ---------------------------------------------------------------------------
# Bastion host and its key pair
# ---------------------------------------------------------------------------
import {
  to = aws_key_pair.bastion
  id = "aerotracker-key"
}

import {
  to = aws_instance.bastion
  id = "i-07148aa263ecf53c5"
}

# ---------------------------------------------------------------------------
# Database networking
#
# The RDS instance itself is NOT imported: it was deleted during the shutdown
# and is declared in database.tf as a restore from its final snapshot.
# ---------------------------------------------------------------------------
import {
  to = aws_db_subnet_group.main
  id = "aerotracker-db-subnet-group"
}
