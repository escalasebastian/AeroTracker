# ==============================================================================
# AeroTracker Phase 7.1 — AWS Infrastructure Deployment (PowerShell)
# ==============================================================================
# This script provisions:
# 1. Custom VPC (10.0.0.0/16) in region eu-west-1
# 2. 2 Public Subnets (AZ-a, AZ-b) + 2 Private Subnets (AZ-a, AZ-b)
# 3. Internet Gateway & Public Route Table
# 4. Security Groups for EC2 and RDS PostgreSQL
# 5. DB Subnet Group & RDS PostgreSQL 16 Instance (db.t3.micro, Free Tier)
# ==============================================================================

param (
    [Parameter(Mandatory=$true)]
    [string]$MasterUserPassword
)

$ErrorActionPreference = "Stop"

$REGION = "eu-west-1"
$VPC_CIDR = "10.0.0.0/16"
$PUB_SUB1_CIDR = "10.0.1.0/24"
$PUB_SUB1_AZ = "eu-west-1a"
$PUB_SUB2_CIDR = "10.0.2.0/24"
$PUB_SUB2_AZ = "eu-west-1b"
$PRIV_SUB1_CIDR = "10.0.10.0/24"
$PRIV_SUB1_AZ = "eu-west-1a"
$PRIV_SUB2_CIDR = "10.0.20.0/24"
$PRIV_SUB2_AZ = "eu-west-1b"

Write-Host "[INFO] Starting AeroTracker AWS Infrastructure deployment in region $REGION..." -ForegroundColor Green

# ------------------------------------------------------------------------------
# 1. Create VPC
# ------------------------------------------------------------------------------
Write-Host "[INFO] Provisioning VPC ($VPC_CIDR)..." -ForegroundColor Cyan
$vpcId = (aws ec2 create-vpc --cidr-block $VPC_CIDR --region $REGION --query 'Vpc.VpcId' --output text)
aws ec2 create-tags --resources $vpcId --tags Key=Name,Value=aerotracker-vpc --region $REGION
aws ec2 modify-vpc-attribute --vpc-id $vpcId --enable-dns-hostnames '{"Value":true}' --region $REGION
Write-Host "[SUCCESS] VPC Created: $vpcId" -ForegroundColor Green

# ------------------------------------------------------------------------------
# 2. Create Internet Gateway
# ------------------------------------------------------------------------------
Write-Host "[INFO] Provisioning Internet Gateway..." -ForegroundColor Cyan
$igwId = (aws ec2 create-internet-gateway --region $REGION --query 'InternetGateway.InternetGatewayId' --output text)
aws ec2 create-tags --resources $igwId --tags Key=Name,Value=aerotracker-igw --region $REGION
aws ec2 attach-internet-gateway --vpc-id $vpcId --internet-gateway-id $igwId --region $REGION
Write-Host "[SUCCESS] Internet Gateway Attached: $igwId" -ForegroundColor Green

# ------------------------------------------------------------------------------
# 3. Create Subnets
# ------------------------------------------------------------------------------
Write-Host "[INFO] Provisioning Subnets..." -ForegroundColor Cyan

# Public Subnet 1
$pubSub1Id = (aws ec2 create-subnet --vpc-id $vpcId --cidr-block $PUB_SUB1_CIDR --availability-zone $PUB_SUB1_AZ --region $REGION --query 'Subnet.SubnetId' --output text)
aws ec2 create-tags --resources $pubSub1Id --tags Key=Name,Value=aerotracker-pub-sub-1a --region $REGION
aws ec2 modify-subnet-attribute --subnet-id $pubSub1Id --map-public-ip-on-launch --region $REGION

# Public Subnet 2
$pubSub2Id = (aws ec2 create-subnet --vpc-id $vpcId --cidr-block $PUB_SUB2_CIDR --availability-zone $PUB_SUB2_AZ --region $REGION --query 'Subnet.SubnetId' --output text)
aws ec2 create-tags --resources $pubSub2Id --tags Key=Name,Value=aerotracker-pub-sub-1b --region $REGION
aws ec2 modify-subnet-attribute --subnet-id $pubSub2Id --map-public-ip-on-launch --region $REGION

# Private Subnet 1
$privSub1Id = (aws ec2 create-subnet --vpc-id $vpcId --cidr-block $PRIV_SUB1_CIDR --availability-zone $PRIV_SUB1_AZ --region $REGION --query 'Subnet.SubnetId' --output text)
aws ec2 create-tags --resources $privSub1Id --tags Key=Name,Value=aerotracker-priv-sub-1a --region $REGION

# Private Subnet 2
$privSub2Id = (aws ec2 create-subnet --vpc-id $vpcId --cidr-block $PRIV_SUB2_CIDR --availability-zone $PRIV_SUB2_AZ --region $REGION --query 'Subnet.SubnetId' --output text)
aws ec2 create-tags --resources $privSub2Id --tags Key=Name,Value=aerotracker-priv-sub-1b --region $REGION

Write-Host "[SUCCESS] Subnets Created:" -ForegroundColor Green
Write-Host "  Public 1:  $pubSub1Id ($PUB_SUB1_AZ)"
Write-Host "  Public 2:  $pubSub2Id ($PUB_SUB2_AZ)"
Write-Host "  Private 1: $privSub1Id ($PRIV_SUB1_AZ)"
Write-Host "  Private 2: $privSub2Id ($PRIV_SUB2_AZ)"

# ------------------------------------------------------------------------------
# 4. Route Table for Public Subnets
# ------------------------------------------------------------------------------
Write-Host "[INFO] Configuring Public Route Table..." -ForegroundColor Cyan
$routeTableId = (aws ec2 create-route-table --vpc-id $vpcId --region $REGION --query 'RouteTable.RouteTableId' --output text)
aws ec2 create-tags --resources $routeTableId --tags Key=Name,Value=aerotracker-public-rt --region $REGION
aws ec2 create-route --route-table-id $routeTableId --destination-cidr-block 0.0.0.0/0 --gateway-id $igwId --region $REGION | Out-Null
aws ec2 associate-route-table --subnet-id $pubSub1Id --route-table-id $routeTableId --region $REGION | Out-Null
aws ec2 associate-route-table --subnet-id $pubSub2Id --route-table-id $routeTableId --region $REGION | Out-Null
Write-Host "[SUCCESS] Route Table Configured: $routeTableId" -ForegroundColor Green

# ------------------------------------------------------------------------------
# 5. Security Groups
# ------------------------------------------------------------------------------
Write-Host "[INFO] Provisioning Security Groups..." -ForegroundColor Cyan

# EC2 Security Group
$ec2SgId = (aws ec2 create-security-group --group-name aerotracker-ec2-sg --description "Security Group for AeroTracker EC2 instance" --vpc-id $vpcId --region $REGION --query 'GroupId' --output text)
aws ec2 create-tags --resources $ec2SgId --tags Key=Name,Value=aerotracker-ec2-sg --region $REGION

# Authorize SSH (22) & HTTP (8080) on EC2 Security Group
aws ec2 authorize-security-group-ingress --group-id $ec2SgId --protocol tcp --port 22 --cidr 0.0.0.0/0 --region $REGION | Out-Null
aws ec2 authorize-security-group-ingress --group-id $ec2SgId --protocol tcp --port 8080 --cidr 0.0.0.0/0 --region $REGION | Out-Null

# RDS Security Group
$rdsSgId = (aws ec2 create-security-group --group-name aerotracker-rds-sg --description "Security Group for AeroTracker RDS PostgreSQL" --vpc-id $vpcId --region $REGION --query 'GroupId' --output text)
aws ec2 create-tags --resources $rdsSgId --tags Key=Name,Value=aerotracker-rds-sg --region $REGION

# Authorize PostgreSQL (5432) ONLY from EC2 Security Group
aws ec2 authorize-security-group-ingress --group-id $rdsSgId --protocol tcp --port 5432 --source-group $ec2SgId --region $REGION | Out-Null

Write-Host "[SUCCESS] Security Groups Created:" -ForegroundColor Green
Write-Host "  EC2 SG: $ec2SgId (Ports 22, 8080 open)"
Write-Host "  RDS SG: $rdsSgId (Port 5432 allowed only from EC2 SG)"

# ------------------------------------------------------------------------------
# 6. DB Subnet Group
# ------------------------------------------------------------------------------
Write-Host "[INFO] Creating DB Subnet Group for RDS..." -ForegroundColor Cyan
aws rds create-db-subnet-group --db-subnet-group-name aerotracker-db-subnet-group --db-subnet-group-description "Subnet group for AeroTracker RDS" --subnet-ids $privSub1Id $privSub2Id --region $REGION | Out-Null
Write-Host "[SUCCESS] DB Subnet Group Created: aerotracker-db-subnet-group" -ForegroundColor Green

# ------------------------------------------------------------------------------
# 7. Create RDS PostgreSQL Instance
# ------------------------------------------------------------------------------
Write-Host "[INFO] Provisioning RDS PostgreSQL 16 instance (db.t3.micro - Free Tier)..." -ForegroundColor Cyan
aws rds create-db-instance `
    --db-instance-identifier aerotracker-db `
    --db-name aerotracker `
    --engine postgres `
    --engine-version 16.10 `
    --db-instance-class db.t3.micro `
    --allocated-storage 20 `
    --storage-type gp2 `
    --master-username aerotracker_admin `
    --master-user-password $MasterUserPassword `
    --vpc-security-group-ids $rdsSgId `
    --db-subnet-group-name aerotracker-db-subnet-group `
    --no-publicly-accessible `
    --backup-retention-period 1 `
    --region $REGION | Out-Null

Write-Host "[SUCCESS] Infrastructure deployment initiated. RDS is initializing in the background." -ForegroundColor Green
Write-Host "  Instance Identifier: aerotracker-db" -ForegroundColor Yellow
Write-Host "  Database Name:       aerotracker" -ForegroundColor Yellow
Write-Host "  Master Username:     aerotracker_admin" -ForegroundColor Yellow
Write-Host "  Check status using:  aws rds describe-db-instances --db-instance-identifier aerotracker-db --region $REGION" -ForegroundColor Cyan
