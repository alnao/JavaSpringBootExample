# Task Fargate che inizializza Aurora con script/init-database/init-mysql.sql
# (sostituisce, per questo stack, script/aws-ecs/run-ecs-mysql-insert.sh che
# scaricava l'SQL da GitHub). L'SQL viene letto dal file locale all'apply e
# incorporato in base64 nel comando: se cambia, nasce una nuova revisione.
# Il task lo lancia start-all.sh con "ecs run-task" PRIMA di creare il service,
# e ne controlla l'exit code.

locals {
  init_sql_b64 = base64encode(file("${path.module}/../init-database/init-mysql.sql"))

  # Esito: 1 se Aurora non risponde o se alla fine manca la tabella users;
  # "--force" fa proseguire mysql sugli oggetti gia' esistenti (indici senza
  # IF NOT EXISTS), cosi' il task e' rilanciabile su un DB gia' inizializzato.
  init_script = <<-EOT
    set -e
    echo "Verifica connessione ad Aurora $DB_HOST..."
    mysql -h "$DB_HOST" -u"$DB_USER" -e 'SELECT 1' > /dev/null || { echo "ERRORE: Aurora non raggiungibile"; exit 1; }
    echo '${local.init_sql_b64}' | base64 -d > /tmp/init-mysql.sql
    echo "Esecuzione di init-mysql.sql ($(wc -c < /tmp/init-mysql.sql) byte)..."
    mysql --force -h "$DB_HOST" -u"$DB_USER" < /tmp/init-mysql.sql || echo "Errori ignorati con --force (oggetti gia' esistenti)"
    mysql -h "$DB_HOST" -u"$DB_USER" -e "SELECT COUNT(*) AS utenti FROM ${var.db_name}.users"
    echo "Database ${var.db_name} inizializzato."
  EOT
}

resource "aws_ecs_task_definition" "init" {
  family                   = local.init_task_family
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.execution.arn # pull da Docker Hub e log; nessun task role

  container_definitions = jsonencode([
    {
      name    = "mysql-init"
      image   = "mysql:8.0"
      command = ["/bin/sh", "-c", local.init_script]
      environment = [
        { name = "DB_HOST", value = aws_rds_cluster.this.endpoint },
        { name = "DB_USER", value = var.db_username },
        { name = "MYSQL_PWD", value = var.db_password }, # letta dal client mysql, non compare nel comando
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.app.name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "init"
        }
      }
    }
  ])

  # "terraform apply -target" di questa risorsa trascina tutto cio' che serve al
  # run-task: istanza Aurora disponibile (non solo il cluster), cluster ECS, ruoli,
  # log group e le REGOLE del security group. Queste ultime vanno dichiarate: il
  # provider toglie l'egress di default alla creazione del SG e, senza la regola
  # di egress e la 3306 interna, il task non raggiunge ne' CloudWatch/Docker Hub ne' Aurora.
  # Idem per le policy del ruolo di execution: gli attachment dipendono dal ruolo,
  # non viceversa, e senza di essi il task non puo' creare il log stream (AccessDenied).
  depends_on = [
    aws_rds_cluster_instance.this,
    aws_ecs_cluster.this,
    aws_vpc_security_group_egress_rule.all,
    aws_vpc_security_group_ingress_rule.internal,
    aws_iam_role_policy_attachment.execution,
  ]

  tags = { Name = local.init_task_family }
}
