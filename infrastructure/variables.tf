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
  default = "test"
}

variable "ilbIp" {}

# region app config

variable "s2s_name" {
  default = "send_letter_consumer"
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
  default = "0 30 0 * * *"
}

variable "subscription" {
}

variable "tenant_id" {}

variable "client_id" {
  description = "(Required) The object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies. This is usually sourced from environment variables and not normally required to be specified."
}

variable "jenkins_AAD_objectId" {
  type        = "string"
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "additional_host_name" {
  default = "send-letter-consumer.platform.hmcts.net"
}

# endregion
