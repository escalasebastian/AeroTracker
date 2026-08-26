# Database networking. The RDS instance itself is declared separately.

resource "aws_db_subnet_group" "main" {
  description = "Subnet group for AeroTracker RDS"
  name        = "aerotracker-db-subnet-group"
  subnet_ids  = ["subnet-0a0367c239013c3da", "subnet-0d1a310b260390dc4"]
  tags        = {}
  tags_all    = {}
}
