# Aurora MySQL: cluster + una istanza raggiungibile pubblicamente (come lo
# script bash). Il database e le tabelle li crea init-mysql.sql dallo user data
# della EC2, per questo non si passa database_name.

resource "aws_rds_cluster" "this" {
  cluster_identifier     = local.db_cluster_id
  engine                 = "aurora-mysql"
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
  publicly_accessible = true
  apply_immediately   = true

  tags = { Name = local.db_instance_id }
}
