variable "gcp_project" {
  description = "The GCP project id"
}

variable "kube_namespace" {
  description = "The Kubernetes namespace"
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

variable "service_account_cloudsql_role" {
  description = "cloudsql client role"
  default     = "roles/cloudsql.client"
}

variable "service_account_pubsub_role" {
  description = "pubsub editor role"
  default     = "roles/pubsub.editor"
}

variable "service_account_blobstore_role" {
  description = "storage objects create role"
  default     = "roles/storage.objects.create"
}

variable "load_config_file" {
  description = "Do not load kube config file"
  default     = false
}

variable ror-uttu-db-password {
  description = "Uttu database password"
}
