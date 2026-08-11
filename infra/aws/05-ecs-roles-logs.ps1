# ==============================================================================
# AeroTracker Phase 7.3 — Step 1: IAM Execution Role & CloudWatch Log Groups
# ==============================================================================
# This script provisions:
# 1. IAM Execution Role ('ecsTaskExecutionRole') with AmazonECSTaskExecutionRolePolicy
# 2. CloudWatch Log Groups with 7-day retention for all 5 microservices
# ==============================================================================

$ErrorActionPreference = "Stop"

$REGION = "eu-west-1"
$ROLE_NAME = "ecsTaskExecutionRole"
$LOG_GROUPS = @(
    "/ecs/aerotracker-api",
    "/ecs/aerotracker-scheduler",
    "/ecs/aerotracker-price-checker",
    "/ecs/aerotracker-notification",
    "/ecs/aerotracker-rabbitmq"
)

Write-Host "[INFO] Starting ECS IAM and CloudWatch setup in $REGION..." -ForegroundColor Green

# ------------------------------------------------------------------------------
# 1. Create or Verify IAM Role for ECS Task Execution
# ------------------------------------------------------------------------------
Write-Host "[INFO] Checking IAM role '$ROLE_NAME'..." -ForegroundColor Cyan

$roleCheck = aws iam get-role --role-name $ROLE_NAME 2>$null

if (-not $roleCheck) {
    Write-Host "[INFO] Creating IAM role '$ROLE_NAME'..." -ForegroundColor Cyan
    
    $trustPolicy = @"
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
"@
    
    aws iam create-role `
        --role-name $ROLE_NAME `
        --assume-role-policy-document $trustPolicy `
        --description "Execution role for AeroTracker ECS Fargate tasks" | Out-Null
        
    Write-Host "[SUCCESS] IAM Role '$ROLE_NAME' created." -ForegroundColor Green
} else {
    Write-Host "[INFO] IAM Role '$ROLE_NAME' already exists." -ForegroundColor Yellow
}

# Attach standard AmazonECSTaskExecutionRolePolicy
Write-Host "[INFO] Attaching AmazonECSTaskExecutionRolePolicy..." -ForegroundColor Cyan
aws iam attach-role-policy `
    --role-name $ROLE_NAME `
    --policy-arn "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"

Write-Host "[SUCCESS] IAM Role configured with execution policy." -ForegroundColor Green

# ------------------------------------------------------------------------------
# 2. Create CloudWatch Log Groups
# ------------------------------------------------------------------------------
Write-Host "[INFO] Provisioning CloudWatch Log Groups..." -ForegroundColor Cyan

foreach ($logGroup in $LOG_GROUPS) {
    $existing = aws logs describe-log-groups --log-group-name-prefix $logGroup --region $REGION --query "logGroups[?logGroupName=='$logGroup'].logGroupName" --output text
    
    if (-not $existing -or $existing -eq "") {
        aws logs create-log-group --log-group-name $logGroup --region $REGION
        Write-Host "  [CREATED] $logGroup" -ForegroundColor Green
    } else {
        Write-Host "  [EXISTS]  $logGroup" -ForegroundColor Yellow
    }
    
    # Set retention to 7 days to conserve Free Tier storage
    aws logs put-retention-policy --log-group-name $logGroup --retention-in-days 7 --region $REGION
}

Write-Host "[SUCCESS] CloudWatch Log Groups configured with 7-day retention." -ForegroundColor Green
Write-Host "[INFO] Step 1 completed successfully." -ForegroundColor Green
