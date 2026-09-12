# AWS Cloud Map private DNS namespace backing ECS Service Discovery.
# Services resolve RabbitMQ through rabbitmq.aerotracker.local.
#
# The namespace is backed by a Route 53 private hosted zone, billed monthly, so
# it only exists while var.platform_enabled is true.

resource "aws_service_discovery_service" "rabbitmq" {
  count = var.platform_enabled ? 1 : 0

  description = null
  # Deregisters any instance the stopping RabbitMQ task left behind, which would
  # otherwise make the deletion fail while switching the platform off.
  force_destroy = true
  name          = "rabbitmq"
  namespace_id  = aws_service_discovery_private_dns_namespace.main[0].id
  tags          = {}
  tags_all      = {}
  dns_config {
    namespace_id   = aws_service_discovery_private_dns_namespace.main[0].id
    routing_policy = "MULTIVALUE"
    dns_records {
      ttl  = 60
      type = "A"
    }
  }
}

resource "aws_service_discovery_private_dns_namespace" "main" {
  count = var.platform_enabled ? 1 : 0

  description = null
  name        = "aerotracker.local"
  tags        = {}
  tags_all    = {}
  vpc         = aws_vpc.main.id
}
