# Bastion host used to open an SSH tunnel into the private RDS subnet.
# Normally kept stopped.

resource "aws_key_pair" "bastion" {
  key_name   = "aerotracker-key"
  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC07V/fPZTxYlgMTzkm00JmcjeuCaMZtOWWAiq/zBKkYvoADNYhqdn5ETaE787fjuVG5xlq5z8DUhQkx1onRsLoGGahW4CQmZ0W6NLlQ1GOzFqFR0PI+TySNpWsllzkyobsWgLFn0ghyg0sMPGaFCQLaukxNtxB0Wh0uTMdtf9ukklK9bGIZqJgt21RBEDzknG+a8fN4pWzISne45RSntz28+eq9D3n/TbL4BgGUnjmIFLI/kmcy/WUR3g+FOQ//OVhUjpX9Lps9ay/MFRyIqOXMBF+S7xNrShNCgxtDIFdRGcWqavJxON/LaDFkenyUCFY4jItzTzWw4v2fNqqET7h aerotracker-key"
  tags       = {}
  tags_all   = {}

  lifecycle {
    # AWS never returns the key material on import, so any value here would
    # force the key pair to be recreated and break the existing .pem.
    ignore_changes = [public_key]
  }

}

resource "aws_instance" "bastion" {
  ami                                  = "ami-08c7a4b4f234dfa77"
  associate_public_ip_address          = false
  availability_zone                    = "eu-west-1a"
  disable_api_stop                     = false
  disable_api_termination              = false
  ebs_optimized                        = false
  get_password_data                    = false
  hibernation                          = false
  instance_initiated_shutdown_behavior = "stop"
  instance_type                        = "t3.micro"
  key_name                             = "aerotracker-key"
  monitoring                           = false
  placement_partition_number           = 0
  private_ip                           = "10.0.1.50"
  secondary_private_ips                = []
  security_groups                      = []
  source_dest_check                    = true
  subnet_id                            = aws_subnet.public_1a.id
  tags = {
    Name = "aerotracker-ec2"
  }
  tags_all = {
    Name = "aerotracker-ec2"
  }
  tenancy                = "default"
  volume_tags            = null
  vpc_security_group_ids = [aws_security_group.ecs.id]
  capacity_reservation_specification {
    capacity_reservation_preference = "open"
  }
  cpu_options {
    core_count       = 1
    threads_per_core = 2
  }
  credit_specification {
    cpu_credits = "unlimited"
  }
  enclave_options {
    enabled = false
  }
  maintenance_options {
    auto_recovery = "default"
  }
  metadata_options {
    http_endpoint               = "enabled"
    http_protocol_ipv6          = "disabled"
    http_put_response_hop_limit = 2
    http_tokens                 = "required"
    instance_metadata_tags      = "disabled"
  }
  private_dns_name_options {
    enable_resource_name_dns_a_record    = false
    enable_resource_name_dns_aaaa_record = false
    hostname_type                        = "ip-name"
  }
  root_block_device {
    delete_on_termination = true
    encrypted             = false
    iops                  = 3000
    tags                  = {}
    tags_all              = {}
    throughput            = 125
    volume_size           = 20
    volume_type           = "gp3"
  }

  lifecycle {
    # AWS returns a hash of the launch script rather than the script itself.
    ignore_changes = [user_data, user_data_replace_on_change]
  }

}
