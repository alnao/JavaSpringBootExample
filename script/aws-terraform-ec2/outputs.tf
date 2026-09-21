# Output mostrati al termine di start-all.sh: gli stessi valori che lo script
# bash stampa alla fine. Nessun output contiene la password.

output "ec2_public_ip" {
  description = "IP pubblico della EC2 (auto-assegnato dalla VPC di default, non e' un Elastic IP)"
  value       = aws_instance.app.public_ip
}

output "app_url" {
  description = "URL dell'applicazione"
  value       = "http://${aws_instance.app.public_ip}:8080"
}

output "ssh_command" {
  description = "Comando per collegarsi via SSH con la chiave generata"
  value       = "ssh -i ${local_sensitive_file.private_key.filename} ec2-user@${aws_instance.app.public_ip}"
}

output "aurora_endpoint" {
  description = "Endpoint del cluster Aurora MySQL"
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

output "private_key_file" {
  description = "Percorso del file .pem della key pair (ignorato da git, rimosso da stop-all.sh)"
  value       = local_sensitive_file.private_key.filename
}
