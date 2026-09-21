# Variabili dello stack. I wrapper start-all.sh / stop-all.sh le impostano via
# TF_VAR_* partendo dalle stesse variabili di shell degli script bash
# (ENVIRONMENT, DB_PASS, AWS_REGION).

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

variable "instance_type" {
  description = "Tipo dell'istanza EC2 che esegue il container."
  type        = string
  default     = "t3.medium"
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
  description = "Nome del database creato da init-mysql.sql e usato dall'applicazione."
  type        = string
  default     = "gestioneannotazioni"
}

variable "ssh_allowed_cidr" {
  description = "CIDR ammesso sulla porta 22. Se null viene usato l'IP pubblico di chi applica (checkip.amazonaws.com)/32, come fa lo script bash."
  type        = string
  default     = null
}

variable "key_name" {
  description = "Nome della key pair EC2 generata da Terraform; il file .pem viene salvato in questa cartella con lo stesso nome."
  type        = string
  default     = "gestioneannotazioni-terraform-key"
}
