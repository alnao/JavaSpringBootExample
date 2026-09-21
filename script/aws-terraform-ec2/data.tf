# Dati letti dall'account: VPC di default e sue subnet, AMI Amazon Linux 2
# piu' recente, IP pubblico del chiamante per la regola SSH.

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# Stesso filtro di "aws ec2 describe-images" nello script bash
data "aws_ami" "amazon_linux_2" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-2.0.*-x86_64-gp2"]
  }
}

# Interrogato solo se non e' stato passato ssh_allowed_cidr
data "http" "my_ip" {
  count = var.ssh_allowed_cidr == null ? 1 : 0
  url   = "https://checkip.amazonaws.com"
}
