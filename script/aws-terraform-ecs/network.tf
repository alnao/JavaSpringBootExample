# Security group nella VPC di default, condiviso da task Fargate, Aurora e Redis
# come nello script bash: app/https/ssh aperte a tutti, MySQL e Redis solo dal
# security group stesso (equivalente di --source-group).

resource "aws_security_group" "app" {
  name        = local.sg_name
  description = "gestioneannotazioni ECS SG"
  vpc_id      = data.aws_vpc.default.id

  tags = { Name = local.sg_name }
}

# 8080 app, 443 https, 22 ssh: da 0.0.0.0/0 (come nello script bash)
resource "aws_vpc_security_group_ingress_rule" "public" {
  for_each = local.public_ports

  security_group_id = aws_security_group.app.id
  description       = "Porta ${each.value} (${each.key}) aperta a tutti, come nello stack bash"
  ip_protocol       = "tcp"
  from_port         = each.value
  to_port           = each.value
  cidr_ipv4         = "0.0.0.0/0"

  tags = { Name = "${local.sg_name}-${each.key}" }
}

# 3306 MySQL, 6379 Redis: solo dai membri del security group (task Fargate)
resource "aws_vpc_security_group_ingress_rule" "internal" {
  for_each = local.internal_ports

  security_group_id            = aws_security_group.app.id
  description                  = "Porta ${each.value} (${each.key}) solo dal security group stesso"
  ip_protocol                  = "tcp"
  from_port                    = each.value
  to_port                      = each.value
  referenced_security_group_id = aws_security_group.app.id

  tags = { Name = "${local.sg_name}-${each.key}" }
}

# Egress totale: serve ai task per pull delle immagini (ECR, Docker Hub) e per AWS
resource "aws_vpc_security_group_egress_rule" "all" {
  security_group_id = aws_security_group.app.id
  description       = "Tutto il traffico in uscita"
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"

  tags = { Name = "${local.sg_name}-egress" }
}
