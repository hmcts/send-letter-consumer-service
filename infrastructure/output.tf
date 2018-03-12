output "vaultUri" {
  value = "https://send-letter-${var.env}.vault.azure.net/"
}

output "vaultName" {
  value = "send-letter-queue-listen-connection-string"
}