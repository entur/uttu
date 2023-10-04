# Contains main description of bulk of terraform?
terraform {
  required_version = ">= 0.13.2"
}

provider "google" {
  version = "~> 4.84.0"
}
provider "kubernetes" {
  version = ">= 2.13.1"
}

variable "gcp_pubsub_project" {
  default = "The GCP pubsub project gcp2"
}

resource "kubernetes_secret" "uttu-psql-credentials" {
  metadata {
    name = "uttu-psql-credentials"
    namespace = var.kube_namespace
  }

  data = {
    "SPRING_DATASOURCE_PASSWORD" = var.ror-uttu-db-password
    "SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_ORGREGISTER_CLIENT_SECRET" = var.ror-partner-auth0-secret
  }
}



resource "google_sql_database_instance" "db_instance_pg13" {
  name = "uttu-db-pg13"
  project = var.gcp_resources_project
  region = "europe-west1"

  settings {
    tier = var.db_tier
    user_labels = var.labels
    availability_type = "ZONAL"
    backup_configuration {
      enabled = true
    }
    ip_configuration {
      require_ssl = true
    }
  }
  database_version = "POSTGRES_13"
}

resource "google_sql_database" "db_pg13" {
  name = "uttu"
  project = var.gcp_resources_project
  instance = google_sql_database_instance.db_instance_pg13.name
}

resource "google_sql_user" "db-user_pg13" {
  name = "uttu"
  project = var.gcp_resources_project
  instance = google_sql_database_instance.db_instance_pg13.name
  password = var.ror-uttu-db-password
}

# add service account as member to pubsub service in the gcp2 project

resource "google_pubsub_topic_iam_member" "pubsub_topic_iam_member" {
  project = var.gcp_pubsub_project
  topic = var.pubsub_topic_name
  role = var.service_account_pubsub_role
  member = var.uttu_service_account
}