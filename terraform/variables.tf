variable "gcp_resources_project" {
  description = "The GCP project id"
}

variable "kube_namespace" {
  description = "The Kubernetes namespace"
  default     = "uttu"
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
  default     = "db-custom-1-3840"
}

variable "db_backup_enabled" {
  description = "Enabled automated db backup"
  default     = false
}

variable "pubsub_topic_name" {
  description = "PubSub Topic name"
  default     = "FlexibleLinesExportQueue"
}

variable "service_account_pubsub_role" {
  description = "Role of the Service Account - more about roles https://cloud.google.com/pubsub/docs/access-control"
  default     = "roles/pubsub.publisher"
}

variable "uttu_service_account" {
  description = "application service account"
}

