# Security group nella VPC di default, condiviso da EC2, Aurora e Redis come
# nello script bash: porte applicative aperte a tutti, SSH solo dal chiamante.

resource "aws_security_group" "app" {
  name        = local.sg_name
  description = "gestioneannotazioni SG"
  vpc_id      = data.aws_vpc.default.id

  tags = { Name = local.sg_name }
}

# 3306 MySQL, 6379 Redis, 8080 app, 8086 adminer, 8087 dynamodb-admin: da 0.0.0.0/0
resource "aws_vpc_security_group_ingress_rule" "app" {
  for_each = local.ingress_ports

  security_group_id = aws_security_group.app.id
  description       = "Porta ${each.value} (${each.key}) aperta a tutti, come nello stack bash"
  ip_protocol       = "tcp"
  from_port         = each.value
  to_port           = each.value
  cidr_ipv4         = "0.0.0.0/0"

  tags = { Name = "${local.sg_name}-${each.key}" }
}

# 22 SSH solo dall'IP pubblico di chi applica (o dal CIDR passato in ssh_allowed_cidr)
resource "aws_vpc_security_group_ingress_rule" "ssh" {
  security_group_id = aws_security_group.app.id
  description       = "SSH dal chiamante"
  ip_protocol       = "tcp"
  from_port         = 22
  to_port           = 22
  cidr_ipv4         = local.ssh_cidr

  tags = { Name = "${local.sg_name}-ssh" }
}

# Egress totale: la console lo crea da sola, Terraform no
resource "aws_vpc_security_group_egress_rule" "all" {
  security_group_id = aws_security_group.app.id
  description       = "Tutto il traffico in uscita"
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"

  tags = { Name = "${local.sg_name}-egress" }
}
