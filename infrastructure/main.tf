provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "s2s_secret" {
  path = "secret/${var.env}/cc/send-letter-consumer/s2s-secret"
}

data "vault_generic_secret" "ftp_user" {
  path = "secret/${var.env}/cc/send-letter-consumer/ftp-user"
}

data "vault_generic_secret" "ftp_private_key" {
  path = "secret/${var.env}/cc/send-letter-consumer/ftp-private-key"
}

data "vault_generic_secret" "ftp_public_key" {
  path = "secret/${var.env}/cc/send-letter-consumer/ftp-public-key"
}

data "vault_generic_secret" "servicebus_conn_string" {
  path = "secret/${var.env}/cc/send-letter/servicebus-listen-conn-string"
}

locals {
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
}

module "consumer" {
  source   = "git@github.com:contino/moj-module-webapp.git"
  product  = "${var.product}-consumer"
  location = "${var.location}"
  env      = "${var.env}"
  ilbIp    = "${var.ilbIp}"

  app_settings = {

    // pdf
    PDF_SERVICE_URL = "http://cmc-pdf-service-${var.env}.service.${local.aseName}.internal"

    // s2s
    S2S_URL     = "http://betadevbccidams2slb.reform.hmcts.net:80"
    S2S_SECRET  = "${data.vault_generic_secret.s2s_secret.data["value"]}"
    S2S_NAME    = "${var.s2s_name}"

    // azure service bus
    SERVICE_BUS_CONNECTION_STRING = "${data.vault_generic_secret.servicebus_conn_string.data["value"]}"
    SERVICE_BUS_INTERVAL          = "${var.service_bus_interval}"

    // ftp
    FTP_HOSTNAME      = "${var.ftp_hostname}"
    FTP_PORT          = "${var.ftp_port}"
    FTP_FINGERPRINT   = "${var.ftp_fingerprint}"
    FTP_TARGET_FOLDER = "${var.ftp_target_folder}"
    FTP_TARGET_FOLDER = "${var.ftp_reports_folder}"
    FTP_USER          = "${data.vault_generic_secret.ftp_user.data["value"]}"
    FTP_PRIVATE_KEY   = "${data.vault_generic_secret.ftp_private_key.data["value"]}"
    FTP_PUBLIC_KEY    = "${data.vault_generic_secret.ftp_public_key.data["value"]}"
  }
}
