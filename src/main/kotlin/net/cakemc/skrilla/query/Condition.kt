package net.cakemc.skrilla.query// === Data Models ===

sealed class Condition {
    data class Comparison(val field: String, val operator: String, val value: String) : Condition()
    data class Between(val field: String, val low: String, val high: String) : Condition()
}

