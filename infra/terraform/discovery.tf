# AWS Cloud Map private DNS namespace backing ECS Service Discovery.
# Services resolve RabbitMQ through rabbitmq.aerotracker.local.

resource "aws_service_discovery_service" "rabbitmq" {
  description   = null
  force_destroy = null
  name          = "rabbitmq"
  namespace_id  = "ns-wrxowo7ctvzie4iq"
  tags          = {}
  tags_all      = {}
  dns_config {
    namespace_id   = "ns-wrxowo7ctvzie4iq"
    routing_policy = "MULTIVALUE"
    dns_records {
      ttl  = 60
      type = "A"
    }
  }
}

resource "aws_service_discovery_private_dns_namespace" "main" {
  description = null
  name        = "aerotracker.local"
  tags        = {}
  tags_all    = {}
  vpc         = "vpc-035f0d731c4b432e0"
}
