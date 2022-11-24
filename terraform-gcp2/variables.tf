variable "gcp_project" {
  description = "The GCP project id"
}

variable "kube_namespace" {
  description = "The Kubernetes namespace"
  default = "uttu"
}

variable "labels" {
  description = "Labels used in all resources"
  type        = map(string)
  default = {
    manager = "terraform"
    team    = "ror"
    slack   = "talk-ror"
    app     = "uttu"
  }
}


variable "db_tier" {
  description = "Database instance tier"
  default = "db-custom-1-3840"
}

variable "db_backup_enabled" {
  description = "Enabled automated db backup"
  default = false
}

variable ror-uttu-db-password {
  description = "Uttu database password"
}

variable ror-partner-auth0-secret {
  description = "Auth0 client secret for Entur partner tenant"
}