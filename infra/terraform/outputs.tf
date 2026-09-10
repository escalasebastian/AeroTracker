output "vpc_id" {
  description = "ID of the AeroTracker VPC."
  value       = aws_vpc.main.id
}

output "public_subnet_ids" {
  description = "IDs of the public subnets hosting the ECS Fargate tasks."
  value       = [aws_subnet.public_1a.id, aws_subnet.public_1b.id]
}

output "private_subnet_ids" {
  description = "IDs of the private subnets hosting RDS."
  value       = [aws_subnet.private_1a.id, aws_subnet.private_1b.id]
}

output "ecs_cluster_name" {
  description = "Name of the ECS cluster running every AeroTracker service."
  value       = aws_ecs_cluster.main.name
}

output "rds_endpoint" {
  description = "Connection endpoint (host:port) of the RDS PostgreSQL instance. Null while the platform is switched off."
  value       = one(aws_db_instance.main[*].endpoint)
}

output "bastion_public_ip" {
  description = "Public IP of the bastion host, used to open an SSH tunnel into the private RDS subnet."
  value       = aws_instance.bastion.public_ip
}
