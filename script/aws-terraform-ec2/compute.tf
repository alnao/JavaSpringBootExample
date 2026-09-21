# Key pair generata da Terraform (chiave privata salvata in locale come .pem,
# e nello state) e istanza EC2 Amazon Linux 2 che avvia il container tramite
# user data. Nome della key pair e Name della EC2 sono DISTINTI dallo stack bash
# per non sovrascrivere il .pem locale dell'altro stack.

resource "tls_private_key" "ec2" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "ec2" {
  key_name   = var.key_name
  public_key = tls_private_key.ec2.public_key_openssh

  tags = { Name = var.key_name }
}

# Equivalente di "create-key-pair ... > NOME.pem; chmod 400": il file resta in
# questa cartella (ignorato da git tramite *.pem) e viene rimosso da stop-all.sh
resource "local_sensitive_file" "private_key" {
  filename        = "${path.module}/${var.key_name}.pem"
  content         = tls_private_key.ec2.private_key_pem
  file_permission = "0400"
}

resource "aws_instance" "app" {
  ami                    = data.aws_ami.amazon_linux_2.id
  instance_type          = var.instance_type
  key_name               = aws_key_pair.ec2.key_name
  vpc_security_group_ids = [aws_security_group.app.id]
  iam_instance_profile   = aws_iam_instance_profile.ec2.name

  user_data = templatefile("${path.module}/user_data.sh.tftpl", {
    aurora_endpoint = aws_rds_cluster.this.endpoint
    db_user         = var.db_username
    db_pass         = var.db_password
    db_name         = var.db_name
    region          = var.region
    sqs_export_url  = aws_sqs_queue.export.url
    sqs_import_url  = aws_sqs_queue.import.url
    redis_host      = aws_elasticache_cluster.redis.cache_nodes[0].address
    redis_port      = aws_elasticache_cluster.redis.cache_nodes[0].port
    init_sql        = file("${path.module}/../init-database/init-mysql.sql")
  })
  # Lo user data e' "lo script": se cambia, l'istanza viene ricreata (i dati stanno su Aurora/DynamoDB)
  user_data_replace_on_change = true

  # L'endpoint del cluster esiste prima che l'istanza DB sia utilizzabile e lo
  # user data ha solo 3 tentativi: si aspetta che l'istanza Aurora sia available
  # ...e che il security group abbia gia' le sue regole (il provider toglie l'egress di default
  # alla creazione del SG: senza la regola di egress lo user data non scarica Docker ne' l'immagine)
  depends_on = [
    aws_rds_cluster_instance.this,
    aws_vpc_security_group_egress_rule.all,
    aws_vpc_security_group_ingress_rule.app,
    aws_vpc_security_group_ingress_rule.ssh,
    aws_iam_role_policy_attachment.ec2, # il ruolo va usato con le policy gia' attaccate
  ]

  tags = {
    Name               = local.ec2_name
    (local.marker_key) = "true" # marcatore usato da test-aws-terraform-ec2.sh
  }

  # I default_tags del provider non si applicano in modo affidabile ai volumi: merge esplicito
  root_block_device {
    tags = merge(local.common_tags, { Name = "${local.ec2_name}-volume" })
  }
}
