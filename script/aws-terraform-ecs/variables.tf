# Variabili dello stack. I wrapper start-all.sh / stop-all.sh le impostano via
# TF_VAR_* partendo dalle stesse variabili di shell degli script bash
# (ENVIRONMENT, DB_PASS, AWS_REGION, IMAGE_TAG).

variable "region" {
  description = "Region AWS in cui creare lo stack."
  type        = string
  default     = "eu-central-1"
}

variable "environment" {
  description = "Ambiente di riferimento, valore del tag Environment (dev, test, production)."
  type        = string
  default     = "dev"

  validation {
    condition     = contains(["dev", "test", "production"], var.environment)
    error_message = "Valore non ammesso per environment: usare dev, test o production."
  }
}

variable "db_engine_version" {
  description = "Versione del motore aurora-mysql. null = versione corrente di AWS (oggi 8.0). Lo script bash pinna 5.7.mysql_aurora.2.11.4 (Aurora MySQL 2, fuori dal supporto standard): impostarla qui per replicarlo."
  type        = string
  default     = null
}

variable "db_instance_class" {
  description = "Classe dell'istanza Aurora MySQL."
  type        = string
  default     = "db.t3.medium"
}

variable "db_username" {
  description = "Utente master di Aurora."
  type        = string
  default     = "gestioneannotazioni_user"
}

variable "db_password" {
  description = "Password dell'utente master di Aurora. Nessun default: passarla con TF_VAR_db_password (il wrapper la legge da DB_PASS)."
  type        = string
  sensitive   = true
}

variable "db_name" {
  description = "Nome del database creato da Aurora (database_name) e usato dall'applicazione."
  type        = string
  default     = "gestioneannotazioni"
}

variable "image_tag" {
  description = "Tag dell'immagine nel repository ECR, costruita e pubblicata dal wrapper start-all.sh."
  type        = string
  default     = "latest"
}

variable "task_cpu" {
  description = "CPU del task Fargate dell'applicazione (unita' ECS)."
  type        = string
  default     = "512"
}

variable "task_memory" {
  description = "Memoria del task Fargate dell'applicazione (MiB)."
  type        = string
  default     = "1024"
}

variable "desired_count" {
  description = "Numero di task del service ECS."
  type        = number
  default     = 1
}
