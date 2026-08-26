# Security groups and their rules, adopted as individual rule
# resources so Terraform does not rewrite them.

resource "aws_vpc_security_group_egress_rule" "ecs_all" {
  cidr_ipv4                    = "0.0.0.0/0"
  cidr_ipv6                    = null
  description                  = null
  from_port                    = null
  ip_protocol                  = "-1"
  prefix_list_id               = null
  referenced_security_group_id = null
  security_group_id            = "sg-0fba59a7dae64fb1d"
  tags                         = null
  to_port                      = null
}

resource "aws_vpc_security_group_ingress_rule" "rds_postgres" {
  cidr_ipv4                    = null
  cidr_ipv6                    = null
  description                  = null
  from_port                    = 5432
  ip_protocol                  = "tcp"
  prefix_list_id               = null
  referenced_security_group_id = "sg-0fba59a7dae64fb1d"
  security_group_id            = "sg-0de3edd17d7132dbe"
  tags                         = null
  to_port                      = 5432
}

resource "aws_vpc_security_group_egress_rule" "rds_all" {
  cidr_ipv4                    = "0.0.0.0/0"
  cidr_ipv6                    = null
  description                  = null
  from_port                    = null
  ip_protocol                  = "-1"
  prefix_list_id               = null
  referenced_security_group_id = null
  security_group_id            = "sg-0de3edd17d7132dbe"
  tags                         = null
  to_port                      = null
}

resource "aws_security_group" "rds" {
  description = "Security Group for AeroTracker RDS PostgreSQL"
  egress = [{
    cidr_blocks      = ["0.0.0.0/0"]
    description      = ""
    from_port        = 0
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "-1"
    security_groups  = []
    self             = false
    to_port          = 0
  }]
  ingress = [{
    cidr_blocks      = []
    description      = ""
    from_port        = 5432
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "tcp"
    security_groups  = ["sg-0fba59a7dae64fb1d"]
    self             = false
    to_port          = 5432
  }]
  name                   = "aerotracker-rds-sg"
  revoke_rules_on_delete = null
  tags = {
    Name = "aerotracker-rds-sg"
  }
  tags_all = {
    Name = "aerotracker-rds-sg"
  }
  vpc_id = "vpc-035f0d731c4b432e0"
}

resource "aws_vpc_security_group_ingress_rule" "ecs_ssh" {
  cidr_ipv4                    = "0.0.0.0/0"
  cidr_ipv6                    = null
  description                  = null
  from_port                    = 22
  ip_protocol                  = "tcp"
  prefix_list_id               = null
  referenced_security_group_id = null
  security_group_id            = "sg-0fba59a7dae64fb1d"
  tags                         = null
  to_port                      = 22
}

resource "aws_vpc_security_group_ingress_rule" "ecs_amqp" {
  cidr_ipv4                    = null
  cidr_ipv6                    = null
  description                  = null
  from_port                    = 5672
  ip_protocol                  = "tcp"
  prefix_list_id               = null
  referenced_security_group_id = "sg-0fba59a7dae64fb1d"
  security_group_id            = "sg-0fba59a7dae64fb1d"
  tags                         = null
  to_port                      = 5672
}

resource "aws_vpc_security_group_ingress_rule" "ecs_https" {
  cidr_ipv4                    = "0.0.0.0/0"
  cidr_ipv6                    = null
  description                  = null
  from_port                    = 443
  ip_protocol                  = "tcp"
  prefix_list_id               = null
  referenced_security_group_id = null
  security_group_id            = "sg-0fba59a7dae64fb1d"
  tags                         = null
  to_port                      = 443
}

resource "aws_vpc_security_group_ingress_rule" "ecs_app" {
  cidr_ipv4                    = "0.0.0.0/0"
  cidr_ipv6                    = null
  description                  = null
  from_port                    = 8080
  ip_protocol                  = "tcp"
  prefix_list_id               = null
  referenced_security_group_id = null
  security_group_id            = "sg-0fba59a7dae64fb1d"
  tags                         = null
  to_port                      = 8080
}

resource "aws_vpc_security_group_ingress_rule" "ecs_http" {
  cidr_ipv4                    = "0.0.0.0/0"
  cidr_ipv6                    = null
  description                  = null
  from_port                    = 80
  ip_protocol                  = "tcp"
  prefix_list_id               = null
  referenced_security_group_id = null
  security_group_id            = "sg-0fba59a7dae64fb1d"
  tags                         = null
  to_port                      = 80
}

resource "aws_security_group" "ecs" {
  description = "Security Group for AeroTracker EC2 instance"
  egress = [{
    cidr_blocks      = ["0.0.0.0/0"]
    description      = ""
    from_port        = 0
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "-1"
    security_groups  = []
    self             = false
    to_port          = 0
  }]
  ingress = [{
    cidr_blocks      = ["0.0.0.0/0"]
    description      = ""
    from_port        = 22
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "tcp"
    security_groups  = []
    self             = false
    to_port          = 22
    }, {
    cidr_blocks      = ["0.0.0.0/0"]
    description      = ""
    from_port        = 443
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "tcp"
    security_groups  = []
    self             = false
    to_port          = 443
    }, {
    cidr_blocks      = ["0.0.0.0/0"]
    description      = ""
    from_port        = 8080
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "tcp"
    security_groups  = []
    self             = false
    to_port          = 8080
    }, {
    cidr_blocks      = ["0.0.0.0/0"]
    description      = ""
    from_port        = 80
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "tcp"
    security_groups  = []
    self             = false
    to_port          = 80
    }, {
    cidr_blocks      = []
    description      = ""
    from_port        = 5672
    ipv6_cidr_blocks = []
    prefix_list_ids  = []
    protocol         = "tcp"
    security_groups  = []
    self             = true
    to_port          = 5672
  }]
  name                   = "aerotracker-ec2-sg"
  revoke_rules_on_delete = null
  tags = {
    Name = "aerotracker-ec2-sg"
  }
  tags_all = {
    Name = "aerotracker-ec2-sg"
  }
  vpc_id = "vpc-035f0d731c4b432e0"
}
