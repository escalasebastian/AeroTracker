# CloudWatch log groups, dashboard and alarms, plus the email alerts.

# The topic and its email subscription exist permanently, so the address is
# confirmed once instead of after every start-up. Neither costs anything idle.
resource "aws_sns_topic" "alerts" {
  name = "aerotracker-alerts"
}

resource "aws_sns_topic_subscription" "alerts_email" {
  topic_arn = aws_sns_topic.alerts.arn
  protocol  = "email"
  endpoint  = var.alert_email
}

locals {
  ecs_service_names = {
    api           = aws_ecs_service.api.name
    scheduler     = aws_ecs_service.scheduler.name
    price-checker = aws_ecs_service.price_checker.name
    notification  = aws_ecs_service.notification.name
    rabbitmq      = one(aws_ecs_service.rabbitmq[*].name)
  }
}

# One alarm per service that emails when the service stops running. ECS
# publishes a free CPU datapoint every minute for each running service, so the
# condition itself (fewer than one sample) can never be true: the alarm is OK
# while datapoints arrive, and moves to INSUFFICIENT_DATA after five minutes
# without any, which is what happens when the task is gone. Only that
# transition sends an email. A new alarm also starts in INSUFFICIENT_DATA, but
# that is its initial state rather than a transition, so start-ups stay silent.
#
# The alarms only exist while the platform is enabled; otherwise every service
# would look down permanently.
resource "aws_cloudwatch_metric_alarm" "service_down" {
  for_each = var.platform_enabled ? local.ecs_service_names : {}

  alarm_name        = "AeroTracker-${each.key}-Down"
  alarm_description = "The ${each.key} service has reported no metrics for 5 minutes: its task is not running."
  namespace         = "AWS/ECS"
  metric_name       = "CPUUtilization"
  dimensions = {
    ClusterName = aws_ecs_cluster.main.name
    ServiceName = each.value
  }
  statistic                 = "SampleCount"
  period                    = 60
  evaluation_periods        = 5
  comparison_operator       = "LessThanThreshold"
  threshold                 = 1
  treat_missing_data        = "missing"
  insufficient_data_actions = [aws_sns_topic.alerts.arn]
}

resource "aws_cloudwatch_log_group" "price_checker" {
  kms_key_id        = null
  log_group_class   = "STANDARD"
  name              = "/ecs/aerotracker-price-checker"
  retention_in_days = 7
  skip_destroy      = false
  tags              = {}
  tags_all          = {}
}

resource "aws_cloudwatch_metric_alarm" "high_cpu" {
  actions_enabled     = true
  alarm_actions       = []
  alarm_description   = "Alarm when ECS CPU exceeds 80%"
  alarm_name          = "AeroTracker-High-CPU"
  comparison_operator = "GreaterThanThreshold"
  dimensions = {
    ClusterName = "aerotracker-cluster"
  }
  evaluation_periods        = 1
  extended_statistic        = null
  insufficient_data_actions = []
  metric_name               = "CPUUtilization"
  namespace                 = "AWS/ECS"
  ok_actions                = []
  period                    = 300
  statistic                 = "Average"
  tags                      = {}
  tags_all                  = {}
  threshold                 = 80
  threshold_metric_id       = null
  treat_missing_data        = "missing"
  unit                      = null
}

resource "aws_cloudwatch_metric_alarm" "high_memory" {
  actions_enabled     = true
  alarm_actions       = []
  alarm_description   = "Alarm when ECS memory exceeds 85%"
  alarm_name          = "AeroTracker-High-Memory"
  comparison_operator = "GreaterThanThreshold"
  dimensions = {
    ClusterName = "aerotracker-cluster"
  }
  evaluation_periods        = 1
  extended_statistic        = null
  insufficient_data_actions = []
  metric_name               = "MemoryUtilization"
  namespace                 = "AWS/ECS"
  ok_actions                = []
  period                    = 300
  statistic                 = "Average"
  tags                      = {}
  tags_all                  = {}
  threshold                 = 85
  threshold_metric_id       = null
  treat_missing_data        = "missing"
  unit                      = null
}

resource "aws_cloudwatch_log_group" "notification" {
  kms_key_id        = null
  log_group_class   = "STANDARD"
  name              = "/ecs/aerotracker-notification"
  retention_in_days = 7
  skip_destroy      = false
  tags              = {}
  tags_all          = {}
}

resource "aws_cloudwatch_log_group" "rabbitmq" {
  kms_key_id        = null
  log_group_class   = "STANDARD"
  name              = "/ecs/aerotracker-rabbitmq"
  retention_in_days = 7
  skip_destroy      = false
  tags              = {}
  tags_all          = {}
}

resource "aws_cloudwatch_log_group" "api" {
  kms_key_id        = null
  log_group_class   = "STANDARD"
  name              = "/ecs/aerotracker-api"
  retention_in_days = 7
  skip_destroy      = false
  tags              = {}
  tags_all          = {}
}

resource "aws_cloudwatch_log_group" "scheduler" {
  kms_key_id        = null
  log_group_class   = "STANDARD"
  name              = "/ecs/aerotracker-scheduler"
  retention_in_days = 7
  skip_destroy      = false
  tags              = {}
  tags_all          = {}
}

resource "aws_cloudwatch_dashboard" "overview" {
  dashboard_body = jsonencode({
    widgets = [{
      height = 6
      properties = {
        metrics = [["AWS/ECS", "CPUUtilization", "ClusterName", "aerotracker-cluster", {
          color = "#1f77b4"
          stat  = "Average"
        }]]
        period = 60
        region = "eu-west-1"
        stat   = "Average"
        title  = "ECS Cluster CPU Utilization (%)"
        yAxis = {
          left = {
            max = 100
            min = 0
          }
        }
      }
      type  = "metric"
      width = 12
      x     = 0
      y     = 0
      }, {
      height = 6
      properties = {
        metrics = [["AWS/ECS", "MemoryUtilization", "ClusterName", "aerotracker-cluster", {
          color = "#ff7f0e"
          stat  = "Average"
        }]]
        period = 60
        region = "eu-west-1"
        stat   = "Average"
        title  = "ECS Cluster Memory Utilization (%)"
        yAxis = {
          left = {
            max = 100
            min = 0
          }
        }
      }
      type  = "metric"
      width = 12
      x     = 12
      y     = 0
      }, {
      height = 6
      properties = {
        metrics = [["AWS/RDS", "DatabaseConnections", "DBInstanceIdentifier", "aerotracker-db", {
          color = "#2ca02c"
          stat  = "Average"
        }]]
        period = 60
        region = "eu-west-1"
        stat   = "Average"
        title  = "RDS PostgreSQL Active Connections"
        yAxis = {
          left = {
            min = 0
          }
        }
      }
      type  = "metric"
      width = 12
      x     = 0
      y     = 6
      }, {
      height = 6
      properties = {
        metrics = [["AWS/RDS", "CPUUtilization", "DBInstanceIdentifier", "aerotracker-db", {
          color = "#d62728"
          stat  = "Average"
        }]]
        period = 60
        region = "eu-west-1"
        stat   = "Average"
        title  = "RDS PostgreSQL CPU Utilization (%)"
        yAxis = {
          left = {
            max = 100
            min = 0
          }
        }
      }
      type  = "metric"
      width = 12
      x     = 12
      y     = 6
    }]
  })
  dashboard_name = "AeroTracker-Overview"
}
