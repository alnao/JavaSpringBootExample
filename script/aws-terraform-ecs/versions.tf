# Versioni di Terraform e del provider, backend per lo state e provider AWS.
# Stack ECS Fargate del profilo aws, equivalente a script/aws-ecs/start-all.sh.

terraform {
  required_version = ">= 1.10" # use_lockfile del backend S3 richiede 1.10+

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Configurazione PARZIALE del backend: bucket, key, region e use_lockfile
  # arrivano da "terraform init -backend-config=..." (vedi script/aws-tf-init.sh,
  # variabili TF_STATE_BUCKET / TF_STATE_REGION / TF_STATE_KEY). Con TF_STATE_BUCKET
  # vuoto il wrapper genera backend_override.tf con un backend "local" (ignorato da git).
  backend "s3" {}
}

provider "aws" {
  region = var.region

  # Tag comuni a TUTTE le risorse (vedi PlatformAws.md, sezione "Tag delle risorse AWS").
  # Il tag Name e' aggiunto risorsa per risorsa; i task ereditano i tag del service.
  default_tags {
    tags = local.common_tags
  }
}
