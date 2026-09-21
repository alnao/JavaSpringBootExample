# Ruoli IAM dei task Fargate, stessi nomi e policy dello script bash:
# - task role: assunto dal container dell'applicazione (DynamoDB, RDS, SQS)
# - execution role: usato da ECS per pull dell'immagine e log su CloudWatch

data "aws_iam_policy_document" "ecs_tasks_assume" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "task" {
  name               = local.task_role_name
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume.json

  tags = { Name = local.task_role_name }
}

resource "aws_iam_role_policy_attachment" "task" {
  for_each = toset(local.task_policy_arns)

  role       = aws_iam_role.task.name
  policy_arn = each.value
}

resource "aws_iam_role" "execution" {
  name               = local.exec_role_name
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume.json

  tags = { Name = local.exec_role_name }
}

resource "aws_iam_role_policy_attachment" "execution" {
  for_each = toset(local.exec_policy_arns)

  role       = aws_iam_role.execution.name
  policy_arn = each.value
}
