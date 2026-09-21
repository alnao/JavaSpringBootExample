# Cluster ECS, log group, task definition Fargate dell'applicazione e service.
# Task definition e service sono la trasposizione di task-def.json e di
# "ecs create-service" dello script bash: stesse 16 variabili d'ambiente,
# stessi log, un task con IP pubblico nelle subnet della VPC di default.

resource "aws_ecs_cluster" "this" {
  name = local.cluster_name

  tags = { Name = local.cluster_name }
}

resource "aws_cloudwatch_log_group" "app" {
  name = local.log_group_name

  tags = { Name = local.log_group_name }
}

# Variabili d'ambiente del container, nello stesso ordine del heredoc bash.
# Le credenziali AWS vuote fanno usare al SDK il task role.
# RDS_PASSWORD e AWS_RDS_PASSWORD in chiaro nella task definition: e' cosi'
# anche nello stack bash, la gestione via secret e' una voce della Roadmap.
locals {
  app_environment = [
    { name = "AWS_ACCESS_KEY_ID", value = "" },
    { name = "AWS_SECRET_ACCESS_KEY", value = "" },
    { name = "SPRING_PROFILES_ACTIVE", value = "aws" },
    { name = "AWS_REGION", value = var.region },
    { name = "AWS_RDS_URL", value = "jdbc:mysql://${aws_rds_cluster.this.endpoint}:3306/${var.db_name}" },
    { name = "AWS_RDS_USERNAME", value = var.db_username },
    { name = "AWS_RDS_PASSWORD", value = var.db_password },
    { name = "RDS_HOST", value = aws_rds_cluster.this.endpoint },
    { name = "RDS_PORT", value = "3306" },
    { name = "RDS_DATABASE", value = var.db_name },
    { name = "RDS_USERNAME", value = var.db_username },
    { name = "RDS_PASSWORD", value = var.db_password },
    { name = "SQS_EXPORT_QUEUE_URL", value = aws_sqs_queue.export.url },
    { name = "SQS_IMPORT_QUEUE_URL", value = aws_sqs_queue.import.url },
    { name = "REDIS_HOST", value = aws_elasticache_cluster.redis.cache_nodes[0].address },
    { name = "REDIS_PORT", value = tostring(aws_elasticache_cluster.redis.cache_nodes[0].port) },
  ]
}

resource "aws_ecs_task_definition" "app" {
  family                   = local.task_family
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  task_role_arn            = aws_iam_role.task.arn
  execution_role_arn       = aws_iam_role.execution.arn

  # Nota: contiene la password, quindi Terraform mostra tutto il blocco come (sensitive value) nel plan
  container_definitions = jsonencode([
    {
      name  = local.container_name
      image = local.app_image
      portMappings = [
        { containerPort = 8080, protocol = "tcp" }
      ]
      environment = local.app_environment
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.app.name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = { Name = local.task_family }
}

# Il service parte con l'immagine gia' su ECR e il DB gia' inizializzato:
# e' start-all.sh a garantire l'ordine (apply mirato -> build/push -> init -> apply).
resource "aws_ecs_service" "app" {
  name            = local.service_name
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.app.arn # una nuova revisione aggiorna il service (rolling)
  desired_count   = var.desired_count
  launch_type     = "FARGATE"
  propagate_tags  = "SERVICE" # i task ereditano i sei tag del service

  network_configuration {
    subnets          = data.aws_subnets.default.ids
    security_groups  = [aws_security_group.app.id]
    assign_public_ip = true # VPC di default senza NAT: serve per pull da ECR e per raggiungere l'app
  }

  # I task hanno bisogno delle regole del SG (egress per ECR/CloudWatch/AWS, 8080 in ingresso):
  # il SG da solo nasce senza egress, quindi la dipendenza va resa esplicita
  depends_on = [
    aws_vpc_security_group_egress_rule.all,
    aws_vpc_security_group_ingress_rule.public,
    aws_vpc_security_group_ingress_rule.internal,
    aws_iam_role_policy_attachment.task,
    aws_iam_role_policy_attachment.execution,
  ]

  tags = { Name = local.service_name }
}
