# configuration for end-to-end tests

resource "azurerm_key_vault_secret" "s2s-url-for-tests" {
  name      = "s2s-url-for-tests"
  value     = "${local.s2s_url}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "s2s-name-for-tests" {
  name      = "s2s-name-for-tests"
  value     = "send_letter_tests"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "s2s-secret-for-tests" {
  name      = "s2s-secret-for-tests"
  value     = "${data.vault_generic_secret.tests_s2s_secret.data["value"]}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "producer-url-for-tests" {
  name      = "producer-url-for-tests"
  value     = "${local.producer_url}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "ftp-hostname-for-tests" {
  name      = "ftp-hostname-for-tests"
  value     = "${var.ftp_hostname}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "ftp-port-for-tests" {
  name      = "ftp-port-for-tests"
  value     = "${var.ftp_port}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "ftp-fingerprint-for-tests" {
  name      = "ftp-fingerprint-for-tests"
  value     = "${var.ftp_fingerprint}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "ftp-target-folder-for-tests" {
  name      = "ftp-target-folder-for-tests"
  value     = "${var.ftp_target_folder}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "ftp-user-for-tests" {
  name      = "ftp-user-for-tests"
  value     = "${local.ftp_user}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "ftp-private-key-for-tests" {
  name      = "ftp-private-key-for-tests"
  value     = "${local.ftp_private_key}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "ftp-public-key-for-tests" {
  name      = "ftp-public-key-for-tests"
  value     = "${local.ftp_public_key}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}
