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

variable "pdf_service_url" {
  default = "TBD"
}

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
  default = "TBD"
}

# endregion
