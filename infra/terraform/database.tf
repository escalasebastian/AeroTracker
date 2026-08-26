# Database networking. The RDS instance itself is declared separately.

resource "aws_db_subnet_group" "main" {
  description = "Subnet group for AeroTracker RDS"
  name        = "aerotracker-db-subnet-group"
  subnet_ids  = ["subnet-0a0367c239013c3da", "subnet-0d1a310b260390dc4"]
  tags        = {}
  tags_all    = {}
}

resource "aws_db_instance" "main" {
  identifier                = "aerotracker-db"
  instance_class            = "db.t3.micro"
  engine                    = "postgres"
  engine_version            = "16.13"
  db_subnet_group_name      = aws_db_subnet_group.main.name
  vpc_security_group_ids    = [aws_security_group.rds.id]
  snapshot_identifier       = var.db_snapshot_identifier
  storage_type              = "gp2"
  publicly_accessible       = false
  multi_az                  = false
  backup_retention_period   = 1
  skip_final_snapshot       = false
  final_snapshot_identifier = "aerotracker-db-final-manual"
  apply_immediately         = true

  # Restoring from a snapshot ignores these arguments on the AWS side, but
  # Terraform still needs them declared to match the resource schema and to
  # avoid trying to "correct" the restored master password on every plan.
  username = var.db_username
  password = var.db_password

  lifecycle {
    ignore_changes = [snapshot_identifier, password]
  }
}
