variable "product" {
  type    = "string"
  default = "send-letter"
}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "vault_section" {
  type = "string"
  description = "Name of the environment-specific section in Vault key path, i.e. secret/{vault_section}/..."
  default = "dev"
}

variable "ilbIp" {}

# region app config

variable "s2s_name" {
  default = "sendletterconsumer"
}

variable s2s_url {
  type = "string"
  default = "http://betaDevBccidamS2SLB.reform.hmcts.net:80"
}

variable "service_bus_interval" {
  default = "30000"
}

variable "ftp_hostname" {
  default = "cmseft.services.xerox.com"
}

variable "ftp_port" {
  default = "22"
}

variable "ftp_fingerprint" {
  default = "SHA256:gYzreAtWAraVRFsOrcP9SPJq9atn7QxXh9pAauKud2U"
}

variable "ftp_target_folder" {
  default = "TO_XEROX"
}

variable "ftp_smoke_test_target_folder" {
  default = "SMOKE_TEST"
}

variable "ftp_reports_folder" {
  default = "FROM_XEROX"
}

variable "ftp_reports_cron" {
  default = "0 30 * * * *"
}

variable "subscription" {
}

# endregion
