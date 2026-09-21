# Ruolo IAM assunto dalla EC2 (accesso a RDS, DynamoDB, ECR in lettura, SQS)
# e instance profile che lo collega all'istanza. Stesse policy dello script bash.

resource "aws_iam_role" "ec2" {
  name = local.role_name

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })

  tags = { Name = local.role_name }
}

resource "aws_iam_role_policy_attachment" "ec2" {
  for_each = toset(local.policy_arns)

  role       = aws_iam_role.ec2.name
  policy_arn = each.value
}

resource "aws_iam_instance_profile" "ec2" {
  name = local.instance_profile_name
  role = aws_iam_role.ec2.name

  tags = { Name = local.instance_profile_name }
}
