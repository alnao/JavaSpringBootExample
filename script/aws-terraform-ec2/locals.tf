# Nomi delle risorse e valori condivisi. I nomi sono IDENTICI a quelli dello
# stack bash script/aws-ec2 (i nomi delle tabelle sono fissi nel codice):
# i due stack sono quindi mutuamente esclusivi nella stessa region.

locals {
  project = "Annotazioni.aws-terraform-ec2"

  # Tag applicati a tutte le risorse tramite default_tags del provider
  common_tags = {
    Environment = var.environment
    Project     = local.project
    Owner       = "AlNao"
    CostCenter  = "Annotazioni"
    ManagedBy   = "Terraform"
  }

  # Marcatore della EC2, DISTINTO da gestioneannotazioni-app dello stack bash:
  # gli script stop-all.sh / test-aws-ec2.sh di script/aws-ec2 non la vedono.
  marker_key = "gestioneannotazioni-terraform-app"

  # --- nomi identici allo stack bash ---
  role_name               = "gestioneannotazioni-ec2-role"
  instance_profile_name   = "gestioneannotazioni-ec2-profile"
  sg_name                 = "gestioneannotazioni-sg"
  db_cluster_id           = "gestioneannotazioni-cluster"
  db_instance_id          = "gestioneannotazioni-instance"
  table_annotazioni       = "annotazioni"
  table_storico           = "annotazioni_storico"
  table_storico_stati     = "annotazioni_storicoStati"
  sqs_export_queue_name   = "gestioneannotazioni-annotazioni-export"
  sqs_import_queue_name   = "gestioneannotazioni-annotazioni-import"
  cache_subnet_group_name = "gestioneannotazioni-redis-subnet-group"
  redis_cluster_id        = "gestioneannotazioni-redis"

  # --- distinti dallo stack bash (Name della EC2 e file .pem locale) ---
  ec2_name = "gestioneannotazioni-terraform-ec2"

  # Porte aperte a tutti, come nello script: MySQL, Redis, app, adminer, dynamodb-admin
  ingress_ports = {
    mysql          = 3306
    redis          = 6379
    app            = 8080
    adminer        = 8086
    dynamodb_admin = 8087
  }

  # SSH: CIDR esplicito oppure IP pubblico del chiamante
  ssh_cidr = var.ssh_allowed_cidr != null ? var.ssh_allowed_cidr : "${chomp(data.http.my_ip[0].response_body)}/32"

  policy_arns = [
    "arn:aws:iam::aws:policy/AmazonRDSFullAccess",
    "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess",
    "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly",
    "arn:aws:iam::aws:policy/AmazonSQSFullAccess",
  ]
}
