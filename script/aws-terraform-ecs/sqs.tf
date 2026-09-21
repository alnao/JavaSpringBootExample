# Code SQS standard con i default (come "sqs create-queue" senza attributi):
# export = annotazioni inviate dallo scheduler, import = annotazioni da importare.

resource "aws_sqs_queue" "export" {
  name = local.sqs_export_queue_name

  tags = { Name = local.sqs_export_queue_name }
}

resource "aws_sqs_queue" "import" {
  name = local.sqs_import_queue_name

  tags = { Name = local.sqs_import_queue_name }
}
