# CloudWatch log groups, dashboard and alarms.

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
