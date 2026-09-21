# Output usati da start-all.sh (login ECR, run-task di inizializzazione,
# attesa del task) e mostrati all'utente. Nessun output contiene la password.

output "ecr_repository_url" {
  description = "URL del repository ECR: destinazione di docker push"
  value       = aws_ecr_repository.app.repository_url
}

output "ecs_cluster_name" {
  description = "Nome del cluster ECS"
  value       = aws_ecs_cluster.this.name
}

output "ecs_service_name" {
  description = "Nome del service ECS"
  value       = aws_ecs_service.app.name
}

output "app_task_definition_arn" {
  description = "ARN (con revisione) della task definition dell'applicazione"
  value       = aws_ecs_task_definition.app.arn
}

output "init_task_definition" {
  description = "family:revision della task definition di inizializzazione del DB, per ecs run-task"
  value       = "${aws_ecs_task_definition.init.family}:${aws_ecs_task_definition.init.revision}"
}

# Presi dai tags_all della task definition (default_tags + Name) e non dai locals:
# cosi' l'output dipende da una risorsa dell'apply mirato e viene scritto nello state
output "init_task_tags_json" {
  description = "Tag da applicare al task di inizializzazione (formato JSON di ecs run-task --tags)"
  value = jsonencode([
    for k, v in aws_ecs_task_definition.init.tags_all : { key = k, value = v }
  ])
}

output "security_group_id" {
  description = "Security group dei task, di Aurora e di Redis"
  value       = aws_security_group.app.id
}

output "subnet_ids" {
  description = "Subnet della VPC di default usate dai task (separate da virgola)"
  value       = join(",", data.aws_subnets.default.ids)
}

output "aurora_endpoint" {
  description = "Endpoint del cluster Aurora MySQL (privato)"
  value       = aws_rds_cluster.this.endpoint
}

output "redis_endpoint" {
  description = "Endpoint ElastiCache Redis host:porta"
  value       = "${aws_elasticache_cluster.redis.cache_nodes[0].address}:${aws_elasticache_cluster.redis.cache_nodes[0].port}"
}

output "sqs_export_queue_url" {
  description = "URL della coda SQS di export"
  value       = aws_sqs_queue.export.url
}

output "sqs_import_queue_url" {
  description = "URL della coda SQS di import"
  value       = aws_sqs_queue.import.url
}

output "log_group_name" {
  description = "Log group CloudWatch dei task (prefissi ecs/ e init/)"
  value       = aws_cloudwatch_log_group.app.name
}
