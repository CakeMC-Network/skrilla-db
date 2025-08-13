package net.cakemc.skrilla.query

data class Query(
    val selectFields: List<SelectField>,
    val collection: String,
    val conditions: List<Condition> = emptyList(),
    val groupBy: String? = null,
    val orderBy: OrderBy? = null,
    val limit: Int? = null
)
