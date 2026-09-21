# Nomi delle risorse e valori condivisi. I nomi sono IDENTICI a quelli dello
# stack bash script/aws-ecs (e, per SG, DynamoDB, SQS e Redis, anche degli
# stack EC2): questo stack e' mutuamente esclusivo con gli altri nella stessa region.

locals {
  project = "Annotazioni.aws-terraform-ecs"

  # Tag applicati a tutte le risorse tramite default_tags del provider
  common_tags = {
    Environment = var.environment
    Project     = local.project
    Owner       = "AlNao"
    CostCenter  = "Annotazioni"
    ManagedBy   = "Terraform"
  }

  # --- nomi identici allo stack bash script/aws-ecs ---
  ecr_repo_name           = "gestioneannotazioni"
  task_role_name          = "gestioneannotazioni-ecs-task-role"
  exec_role_name          = "gestioneannotazioni-ecs-execution-role"
  sg_name                 = "gestioneannotazioni-sg"
  db_cluster_id           = "gestioneannotazioni-aurora-cluster"
  db_instance_id          = "gestioneannotazioni-aurora-instance"
  table_annotazioni       = "annotazioni"
  table_storico           = "annotazioni_storico"
  table_storico_stati     = "annotazioni_storicoStati"
  sqs_export_queue_name   = "gestioneannotazioni-annotazioni-export"
  sqs_import_queue_name   = "gestioneannotazioni-annotazioni-import"
  cache_subnet_group_name = "gestioneannotazioni-redis-subnet-group"
  redis_cluster_id        = "gestioneannotazioni-redis"
  cluster_name            = "gestioneannotazioni-cluster"
  service_name            = "gestioneannotazioni-service"
  task_family             = "gestioneannotazioni-task"
  container_name          = "gestioneannotazioni"
  log_group_name          = "/ecs/gestioneannotazioni-app"

  # Task di inizializzazione del database (sostituisce run-ecs-mysql-insert.sh)
  init_task_family = "gestioneannotazioni-mysql-init"

  # Immagine dell'applicazione: <account>.dkr.ecr.<region>.amazonaws.com/gestioneannotazioni:<tag>
  app_image = "${aws_ecr_repository.app.repository_url}:${var.image_tag}"

  # Porte del security group, come nello script bash: app/https/ssh da tutti,
  # MySQL e Redis solo dal security group stesso (--source-group)
  public_ports = {
    app   = 8080
    https = 443
    ssh   = 22
  }
  internal_ports = {
    mysql = 3306
    redis = 6379
  }

  task_policy_arns = [
    "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess",
    "arn:aws:iam::aws:policy/AmazonRDSFullAccess",
    "arn:aws:iam::aws:policy/AmazonSQSFullAccess",
  ]
  exec_policy_arns = [
    "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy",
    "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess",
    "arn:aws:iam::aws:policy/AmazonSQSFullAccess",
  ]
}
