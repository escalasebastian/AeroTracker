# ==============================================================================
# AeroTracker Phase 7.2 — AWS EC2 Instance Provisioning (PowerShell)
# ==============================================================================
# This script provisions:
# 1. SSH Key Pair ('aerotracker-key') saved to infra/aws/aerotracker-key.pem
# 2. Ubuntu 24.04 LTS EC2 Instance (t3.micro, Free Tier) in Public Subnet 1a
# 3. Executes user-data.sh to bootstrap Docker, Docker Compose, and 2GB Swap
# ==============================================================================

$ErrorActionPreference = "Stop"

$REGION = "eu-west-1"
$AMI_ID = "ami-08c7a4b4f234dfa77" # Ubuntu 24.04 LTS amd64 in eu-west-1
$INSTANCE_TYPE = "t3.micro"
$KEY_NAME = "aerotracker-key"
$KEY_FILE = "$PSScriptRoot/aerotracker-key.pem"
$USER_DATA_FILE = "$PSScriptRoot/user-data.sh"

Write-Host "[INFO] Starting AeroTracker EC2 Provisioning in $REGION..." -ForegroundColor Green

# ------------------------------------------------------------------------------
# 1. Ensure SSH Key Pair
# ------------------------------------------------------------------------------
Write-Host "[INFO] Checking SSH Key Pair '$KEY_NAME'..." -ForegroundColor Cyan
$existingKey = aws ec2 describe-key-pairs --key-names $KEY_NAME --region $REGION 2>$null

if (-not $existingKey) {
    Write-Host "[INFO] Generating new SSH Key Pair '$KEY_NAME'..." -ForegroundColor Cyan
    $keyMaterial = aws ec2 create-key-pair --key-name $KEY_NAME --query 'KeyMaterial' --output text --region $REGION
    Set-Content -Path $KEY_FILE -Value $keyMaterial -Encoding ASCII
    Write-Host "[SUCCESS] Key pair saved to $KEY_FILE" -ForegroundColor Green
} else {
    Write-Host "[INFO] Key pair '$KEY_NAME' already exists in AWS." -ForegroundColor Yellow
}

# ------------------------------------------------------------------------------
# 2. Discover Subnet and Security Group IDs
# ------------------------------------------------------------------------------
Write-Host "[INFO] Discovering VPC resources..." -ForegroundColor Cyan
$subnetId = (aws ec2 describe-subnets --filters "Name=tag:Name,Values=aerotracker-pub-sub-1a" --region $REGION --query "Subnets[0].SubnetId" --output text)
$securityGroupId = (aws ec2 describe-security-groups --filters "Name=group-name,Values=aerotracker-ec2-sg" --region $REGION --query "SecurityGroups[0].GroupId" --output text)

if (-not $subnetId -or $subnetId -eq "None") {
    throw "Public subnet 'aerotracker-pub-sub-1a' not found. Please verify Phase 7.1 setup."
}
if (-not $securityGroupId -or $securityGroupId -eq "None") {
    throw "Security group 'aerotracker-ec2-sg' not found. Please verify Phase 7.1 setup."
}

Write-Host "  Subnet ID:         $subnetId"
Write-Host "  Security Group ID: $securityGroupId"

# ------------------------------------------------------------------------------
# 3. Launch EC2 Instance
# ------------------------------------------------------------------------------
Write-Host "[INFO] Launching EC2 instance ($INSTANCE_TYPE)..." -ForegroundColor Cyan

$userDataBase64 = [Convert]::ToBase64String([System.IO.File]::ReadAllBytes($USER_DATA_FILE))

$instanceId = (aws ec2 run-instances `
    --image-id $AMI_ID `
    --instance-type $INSTANCE_TYPE `
    --key-name $KEY_NAME `
    --subnet-id $subnetId `
    --security-group-ids $securityGroupId `
    --user-data $userDataBase64 `
    --block-device-mappings '[{"DeviceName":"/dev/sda1","Ebs":{"VolumeSize":20,"VolumeType":"gp3","DeleteOnTermination":true}}]' `
    --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=aerotracker-ec2}]' `
    --region $REGION `
    --query 'Instances[0].InstanceId' `
    --output text)

Write-Host "[SUCCESS] EC2 Instance launched: $instanceId" -ForegroundColor Green
Write-Host "[INFO] Waiting for instance to enter 'running' state..." -ForegroundColor Cyan

aws ec2 wait instance-running --instance-ids $instanceId --region $REGION

$publicIp = (aws ec2 describe-instances --instance-ids $instanceId --region $REGION --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
$publicDns = (aws ec2 describe-instances --instance-ids $instanceId --region $REGION --query 'Reservations[0].Instances[0].PublicDnsName' --output text)

Write-Host "[SUCCESS] EC2 instance is RUNNING!" -ForegroundColor Green
Write-Host "  Instance ID: $instanceId" -ForegroundColor Yellow
Write-Host "  Public IP:   $publicIp" -ForegroundColor Yellow
Write-Host "  Public DNS:  $publicDns" -ForegroundColor Yellow
Write-Host ""
Write-Host "Connect via SSH:" -ForegroundColor Cyan
Write-Host "  ssh -i $KEY_FILE ubuntu@$publicIp" -ForegroundColor White
