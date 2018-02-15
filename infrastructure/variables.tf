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

variable "ilbIp" {}

# region app config

variable "s2s_name" {
  default = "sendletterconsumer"
}

variable "service_bus_interval" {
  default = "30000"
}

variable "ftp_hostname" {
  default = "TBD"
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

variable "subscription" {
}

# endregion
