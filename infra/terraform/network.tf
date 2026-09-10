# VPC, subnets, internet gateway and routing.

resource "aws_route_table_association" "public_1a" {
  gateway_id     = null
  route_table_id = aws_route_table.public.id
  subnet_id      = aws_subnet.public_1a.id
}

resource "aws_subnet" "public_1b" {
  assign_ipv6_address_on_creation                = false
  availability_zone                              = "eu-west-1b"
  cidr_block                                     = "10.0.2.0/24"
  enable_dns64                                   = false
  enable_resource_name_dns_a_record_on_launch    = false
  enable_resource_name_dns_aaaa_record_on_launch = false
  ipv6_cidr_block                                = null
  ipv6_native                                    = false
  map_public_ip_on_launch                        = true
  private_dns_hostname_type_on_launch            = "ip-name"
  tags = {
    Name = "aerotracker-pub-sub-1b"
  }
  tags_all = {
    Name = "aerotracker-pub-sub-1b"
  }
  vpc_id = aws_vpc.main.id
}

resource "aws_route_table_association" "public_1b" {
  gateway_id     = null
  route_table_id = aws_route_table.public.id
  subnet_id      = aws_subnet.public_1b.id
}

resource "aws_subnet" "private_1b" {
  assign_ipv6_address_on_creation                = false
  availability_zone                              = "eu-west-1b"
  cidr_block                                     = "10.0.20.0/24"
  enable_dns64                                   = false
  enable_resource_name_dns_a_record_on_launch    = false
  enable_resource_name_dns_aaaa_record_on_launch = false
  ipv6_cidr_block                                = null
  ipv6_native                                    = false
  map_public_ip_on_launch                        = false
  private_dns_hostname_type_on_launch            = "ip-name"
  tags = {
    Name = "aerotracker-priv-sub-1b"
  }
  tags_all = {
    Name = "aerotracker-priv-sub-1b"
  }
  vpc_id = aws_vpc.main.id
}

resource "aws_internet_gateway" "main" {
  tags = {
    Name = "aerotracker-igw"
  }
  tags_all = {
    Name = "aerotracker-igw"
  }
  vpc_id = aws_vpc.main.id
}

resource "aws_route_table" "public" {
  propagating_vgws = []
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }
  tags = {
    Name = "aerotracker-public-rt"
  }
  tags_all = {
    Name = "aerotracker-public-rt"
  }
  vpc_id = aws_vpc.main.id
}

resource "aws_subnet" "public_1a" {
  assign_ipv6_address_on_creation                = false
  availability_zone                              = "eu-west-1a"
  cidr_block                                     = "10.0.1.0/24"
  enable_dns64                                   = false
  enable_resource_name_dns_a_record_on_launch    = false
  enable_resource_name_dns_aaaa_record_on_launch = false
  ipv6_cidr_block                                = null
  ipv6_native                                    = false
  map_public_ip_on_launch                        = true
  private_dns_hostname_type_on_launch            = "ip-name"
  tags = {
    Name = "aerotracker-pub-sub-1a"
  }
  tags_all = {
    Name = "aerotracker-pub-sub-1a"
  }
  vpc_id = aws_vpc.main.id
}

resource "aws_subnet" "private_1a" {
  assign_ipv6_address_on_creation                = false
  availability_zone                              = "eu-west-1a"
  cidr_block                                     = "10.0.10.0/24"
  enable_dns64                                   = false
  enable_resource_name_dns_a_record_on_launch    = false
  enable_resource_name_dns_aaaa_record_on_launch = false
  ipv6_cidr_block                                = null
  ipv6_native                                    = false
  map_public_ip_on_launch                        = false
  private_dns_hostname_type_on_launch            = "ip-name"
  tags = {
    Name = "aerotracker-priv-sub-1a"
  }
  tags_all = {
    Name = "aerotracker-priv-sub-1a"
  }
  vpc_id = aws_vpc.main.id
}

resource "aws_vpc" "main" {
  assign_generated_ipv6_cidr_block     = false
  cidr_block                           = "10.0.0.0/16"
  enable_dns_hostnames                 = true
  enable_dns_support                   = true
  enable_network_address_usage_metrics = false
  instance_tenancy                     = "default"
  ipv4_ipam_pool_id                    = null
  ipv4_netmask_length                  = null
  tags = {
    Name = "aerotracker-vpc"
  }
  tags_all = {
    Name = "aerotracker-vpc"
  }
}
