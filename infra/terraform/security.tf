# Security groups and their rules.
#
# Rules are managed exclusively as standalone aws_vpc_security_group_*_rule
# resources. Mixing them with inline ingress/egress blocks on the group makes the
# two definitions fight over the same rules, so the groups declare none inline.
#
# Nothing in AeroTracker accepts traffic from the internet: the API reads Telegram
# through long polling, so the only ingress is service-to-service traffic inside
# the VPC, plus optional SSH to the bastion from a single trusted address.

resource "aws_security_group" "ecs" {
  name        = "aerotracker-ec2-sg"
  description = "Security Group for AeroTracker EC2 instance"
  vpc_id      = aws_vpc.main.id
  tags = {
    Name = "aerotracker-ec2-sg"
  }
  tags_all = {
    Name = "aerotracker-ec2-sg"
  }
}

resource "aws_security_group" "rds" {
  name        = "aerotracker-rds-sg"
  description = "Security Group for AeroTracker RDS PostgreSQL"
  vpc_id      = aws_vpc.main.id
  tags = {
    Name = "aerotracker-rds-sg"
  }
  tags_all = {
    Name = "aerotracker-rds-sg"
  }
}

# Outbound traffic: Telegram, SerpApi, GHCR image pulls and SSM secrets.
resource "aws_vpc_security_group_egress_rule" "ecs_all" {
  security_group_id = aws_security_group.ecs.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

# RabbitMQ, reachable only by tasks sharing this group.
resource "aws_vpc_security_group_ingress_rule" "ecs_amqp" {
  security_group_id            = aws_security_group.ecs.id
  referenced_security_group_id = aws_security_group.ecs.id
  ip_protocol                  = "tcp"
  from_port                    = 5672
  to_port                      = 5672
}

# SSH to the bastion is closed unless an address is explicitly allowed through
# var.ssh_allowed_cidr, and only for as long as it stays set.
resource "aws_vpc_security_group_ingress_rule" "ecs_ssh" {
  count = var.ssh_allowed_cidr == null ? 0 : 1

  security_group_id = aws_security_group.ecs.id
  cidr_ipv4         = var.ssh_allowed_cidr
  ip_protocol       = "tcp"
  from_port         = 22
  to_port           = 22
}

# PostgreSQL, reachable only from the application tasks and the bastion.
resource "aws_vpc_security_group_ingress_rule" "rds_postgres" {
  security_group_id            = aws_security_group.rds.id
  referenced_security_group_id = aws_security_group.ecs.id
  ip_protocol                  = "tcp"
  from_port                    = 5432
  to_port                      = 5432
}

resource "aws_vpc_security_group_egress_rule" "rds_all" {
  security_group_id = aws_security_group.rds.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}
