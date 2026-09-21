# Aurora MySQL PRIVATA (l'istanza non e' raggiungibile da internet, come nello
# script bash): vi accedono solo i task Fargate tramite il security group.
# Il database viene creato da Aurora (database_name); tabelle e utenti li crea
# il task di inizializzazione (init-db.tf) con init-mysql.sql.

resource "aws_rds_cluster" "this" {
  cluster_identifier     = local.db_cluster_id
  engine                 = "aurora-mysql"
  engine_version         = var.db_engine_version # null = versione corrente di AWS; lo script bash pinna 5.7
  database_name          = var.db_name
  master_username        = var.db_username
  master_password        = var.db_password
  vpc_security_group_ids = [aws_security_group.app.id]
  skip_final_snapshot    = true # come "delete-db-cluster --skip-final-snapshot" in stop-all.sh
  apply_immediately      = true

  tags = { Name = local.db_cluster_id }
}

resource "aws_rds_cluster_instance" "this" {
  identifier          = local.db_instance_id
  cluster_identifier  = aws_rds_cluster.this.id
  engine              = aws_rds_cluster.this.engine
  engine_version      = aws_rds_cluster.this.engine_version
  instance_class      = var.db_instance_class
  publicly_accessible = false
  apply_immediately   = true

  tags = { Name = local.db_instance_id }
}
