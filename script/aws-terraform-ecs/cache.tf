# ElastiCache Redis per i lock distribuiti: subnet group sulle subnet della
# VPC di default e un cluster a nodo singolo, come nello script bash (Fargate e Aurora nella stessa VPC).

resource "aws_elasticache_subnet_group" "redis" {
  name        = local.cache_subnet_group_name
  description = "Subnet group for gestioneannotazioni Redis"
  subnet_ids  = data.aws_subnets.default.ids

  tags = { Name = local.cache_subnet_group_name }
}

resource "aws_elasticache_cluster" "redis" {
  cluster_id         = local.redis_cluster_id
  engine             = "redis"
  node_type          = "cache.t3.micro"
  num_cache_nodes    = 1
  port               = 6379
  subnet_group_name  = aws_elasticache_subnet_group.redis.name
  security_group_ids = [aws_security_group.app.id]
  apply_immediately  = true

  tags = { Name = local.redis_cluster_id }
}
