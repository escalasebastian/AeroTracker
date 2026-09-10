# Database networking and the RDS instance.
#
# The instance only exists while var.platform_enabled is true. Switching the
# platform off deletes it without keeping a snapshot, so RDS bills nothing in
# between. Each start-up creates an empty database and the api service's Flyway
# migrations rebuild the schema on boot. Data does not survive a shutdown, which
# is acceptable because the platform is only brought up for demos.

resource "aws_db_subnet_group" "main" {
  description = "Subnet group for AeroTracker RDS"
  name        = "aerotracker-db-subnet-group"
  subnet_ids  = [aws_subnet.private_1a.id, aws_subnet.private_1b.id]
  tags        = {}
  tags_all    = {}
}

resource "aws_db_instance" "main" {
  count = var.platform_enabled ? 1 : 0

  identifier             = "aerotracker-db"
  instance_class         = "db.t3.micro"
  engine                 = "postgres"
  engine_version         = "16.13"
  allocated_storage      = 20
  storage_type           = "gp2"
  db_name                = "aerotracker"
  username               = var.db_username
  password               = var.db_password
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false
  multi_az               = false
  apply_immediately      = true

  # No automated backups and no final snapshot: nothing is kept, and nothing is
  # billed, once the platform is switched off.
  backup_retention_period = 0
  skip_final_snapshot     = true
}

locals {
  # The task definitions need a DB_URL even while the database is switched off.
  # No task runs in that state, so the placeholder host is never contacted.
  db_url = var.platform_enabled ? "jdbc:postgresql://${aws_db_instance.main[0].endpoint}/aerotracker" : "jdbc:postgresql://database-disabled/aerotracker"
}
