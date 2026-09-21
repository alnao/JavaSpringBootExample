# Tre tabelle DynamoDB identiche a quelle dello script bash. I nomi sono fissi:
# "annotazioni" e' configurabile via env nell'app, "annotazioni_storicoStati" no.

resource "aws_dynamodb_table" "annotazioni" {
  name         = local.table_annotazioni
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"

  attribute {
    name = "id"
    type = "S"
  }

  tags = { Name = local.table_annotazioni }
}

resource "aws_dynamodb_table" "storico" {
  name         = local.table_storico
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"

  attribute {
    name = "id"
    type = "S"
  }

  tags = { Name = local.table_storico }
}

# Storico dei cambi di stato: chiave idOperazione, indice per annotazione
# ordinato per dataModifica (PROVISIONED 5/5 come nello script)
resource "aws_dynamodb_table" "storico_stati" {
  name           = local.table_storico_stati
  billing_mode   = "PROVISIONED"
  read_capacity  = 5
  write_capacity = 5
  hash_key       = "idOperazione"

  attribute {
    name = "idOperazione"
    type = "S"
  }
  attribute {
    name = "idAnnotazione"
    type = "S"
  }
  attribute {
    name = "dataModifica"
    type = "S"
  }

  global_secondary_index {
    name            = "idAnnotazione-index"
    hash_key        = "idAnnotazione"
    range_key       = "dataModifica"
    projection_type = "ALL"
    read_capacity   = 5
    write_capacity  = 5
  }

  tags = { Name = local.table_storico_stati }
}
