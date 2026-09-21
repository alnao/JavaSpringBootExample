# Repository ECR dell'immagine dell'applicazione. L'immagine viene costruita
# dal Dockerfile del repository e pubblicata dal wrapper start-all.sh
# (docker build + push) dopo un apply mirato di questa sola risorsa.

resource "aws_ecr_repository" "app" {
  name         = local.ecr_repo_name
  force_delete = true # destroy cancella anche le immagini, come "ecr delete-repository --force"

  image_scanning_configuration {
    scan_on_push = false
  }

  tags = { Name = local.ecr_repo_name }
}
