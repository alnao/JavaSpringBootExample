# Dati letti dall'account: VPC di default e sue subnet (usate da Aurora, Redis
# e dai task Fargate), account e region per costruire ARN e URL.

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

data "aws_caller_identity" "current" {}

data "aws_region" "current" {}
