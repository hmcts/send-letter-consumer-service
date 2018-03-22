provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "consumer_s2s_secret" {
  path = "secret/${var.vault_section}/ccidam/service-auth-provider/api/microservice-keys/send-letter-consumer"
}

data "vault_generic_secret" "tests_s2s_secret" {
  path = "secret/${var.vault_section}/ccidam/service-auth-provider/api/microservice-keys/send-letter-tests"
}

data "vault_generic_secret" "ftp_user" {
  path = "secret/${var.vault_section}/cc/send-letter-consumer/ftp-user"
}

data "vault_generic_secret" "ftp_private_key" {
  path = "secret/${var.vault_section}/cc/send-letter-consumer/ftp-private-key"
}

data "vault_generic_secret" "ftp_public_key" {
  path = "secret/${var.vault_section}/cc/send-letter-consumer/ftp-public-key"
}

data "vault_generic_secret" "servicebus_conn_string" {
  path = "secret/${var.vault_section}/cc/send-letter/servicebus-listen-conn-string"
}

locals {
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  s2s_url = "http://rpe-service-auth-provider-${var.env}.service.${local.ase_name}.internal"
  producer_url = "http://send-letter-producer-${var.env}.service.${local.ase_name}.internal"
}

module "consumer" {
  source        = "git@github.com:contino/moj-module-webapp.git"
  product       = "${var.product}-consumer"
  location      = "${var.location}"
  env           = "${var.env}"
  ilbIp         = "${var.ilbIp}"
  subscription  = "${var.subscription}"

  app_settings = {

    // pdf
    PDF_SERVICE_URL = "http://cmc-pdf-service-${var.env}.service.${local.ase_name}.internal"

    // s2s
    S2S_URL     = "${local.s2s_url}"
    S2S_SECRET  = "${data.vault_generic_secret.consumer_s2s_secret.data["value"]}"
    S2S_NAME    = "${var.s2s_name}"

    // azure service bus
    SERVICE_BUS_CONNECTION_STRING = "${data.vault_generic_secret.servicebus_conn_string.data["value"]}"
    SERVICE_BUS_INTERVAL          = "${var.service_bus_interval}"

    // ftp
    FTP_HOSTNAME              = "${var.ftp_hostname}"
    FTP_PORT                  = "${var.ftp_port}"
    FTP_FINGERPRINT           = "${var.ftp_fingerprint}"
    FTP_TARGET_FOLDER         = "${var.ftp_target_folder}"
    FTP_SMOKE_TEST_TARGET_FOLDER = "${var.ftp_smoke_test_target_folder}"
    FTP_REPORTS_FOLDER        = "${var.ftp_reports_folder}"
    FTP_REPORTS_CRON          = "${var.ftp_reports_cron}"
    FTP_USER                  = "${data.vault_generic_secret.ftp_user.data["value"]}"
    SEND_LETTER_PRODUCER_URL  = "http://send-letter-producer-${var.env}.service.${local.ase_name}.internal"
    FTP_PRIVATE_KEY           = "${replace(data.vault_generic_secret.ftp_private_key.data["value"], "\\n", "\n")}"
    FTP_PUBLIC_KEY            = "${replace(data.vault_generic_secret.ftp_public_key.data["value"], "\\n", "\n")}"
  }
}

module "key-vault" {
  source              = "git@github.com:contino/moj-module-key-vault?ref=master"
  product             = "${var.product}-consumer"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.consumer.resource_group_name}"
  # dcd_cc-dev group object ID
  product_group_object_id = "38f9dea6-e861-4a50-9e73-21e64f563537"
}

# region smoke test config
resource "azurerm_key_vault_secret" "s2s-url" {
  name      = "s2s-url"
  value     = "${local.s2s_url}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "test-s2s-name" {
  name      = "test-s2s-name"
  value     = "send_letter_tests"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "test-s2s-secret" {
  name      = "test-s2s-secret"
  value     = "${data.vault_generic_secret.tests_s2s_secret.data["value"]}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "producer-url" {
  name      = "send-letter-producer-url"
  value     = "${local.producer_url}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}
# endregion
