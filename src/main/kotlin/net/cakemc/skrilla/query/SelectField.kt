package net.cakemc.skrilla.query

sealed class SelectField {
    data class Field(val name: String) : SelectField()
    data class Expression(val left: String, val op: String, val right: String, val alias: String) : SelectField()
    data class Aggregate(val function: String, val field: String, val alias: String) : SelectField()
}
