package net.cakemc.skrilla.query

object QueryParser {

    fun tokenize(input: String): List<String> {
        val regex = Regex("""("[^"]*"|\w+\([^)]*\)|\w+|<=|>=|!=|=|<|>|,|\(|\)|\*)""")
        return regex.findAll(input).map { it.value }.toList()
    }

    fun parse(tokens: List<String>): Query {
        var index = 0

        fun expect(expected: String): String {
            if (tokens.getOrNull(index)?.equals(expected, true) != true)
                error("Expected '$expected' but got '${tokens.getOrNull(index)}'")
            return tokens[index++]
        }

        fun parseSelectFields(): List<SelectField> {
            val fields = mutableListOf<SelectField>()
            while (index < tokens.size) {
                if (tokens[index].equals("FROM", true)) break
                if (tokens[index] == ",") { index++; continue }

                val token = tokens[index]

                // Aggregate function? e.g., SUM(kills)
                val aggMatch = Regex("""(\w+)\((\w+)\)""").matchEntire(token)
                if (aggMatch != null) {
                    index++
                    var alias = "${aggMatch.groupValues[1]}(${aggMatch.groupValues[2]})"
                    if (tokens.getOrNull(index)?.equals("AS", true) == true) {
                        index++
                        alias = tokens[index++]
                    }
                    fields += SelectField.Aggregate(aggMatch.groupValues[1].uppercase(), aggMatch.groupValues[2], alias)
                    continue
                }

                // Expression? e.g., kills / deaths
                val left = tokens[index++]
                if (index < tokens.size && listOf("+", "-", "*", "/").contains(tokens[index])) {
                    val op = tokens[index++]
                    val right = tokens[index++]
                    var alias = "$left$op$right"
                    if (tokens.getOrNull(index)?.equals("AS", true) == true) {
                        index++
                        alias = tokens[index++]
                    }
                    fields += SelectField.Expression(left, op, right, alias)
                } else {
                    var name = left
                    if (tokens.getOrNull(index)?.equals("AS", true) == true) {
                        index++
                        name = tokens[index++]
                    }
                    fields += SelectField.Field(name)
                }
            }
            return fields
        }


        fun parseConditions(): List<Condition> {
            val conditions = mutableListOf<Condition>()
            while (index < tokens.size) {
                val field = tokens[index++]
                val op = tokens[index++].uppercase()
                if (op == "BETWEEN") {
                    val low = tokens[index++]
                    expect("AND")
                    val high = tokens[index++]
                    conditions.add(Condition.Between(field, low, high))
                } else {
                    val value = tokens[index++].trim('"')
                    conditions.add(Condition.Comparison(field, op, value))
                }
                if (tokens.getOrNull(index)?.uppercase() == "AND") index++ else break
            }
            return conditions
        }

        expect("SELECT")
        val fields = parseSelectFields()

        expect("FROM")
        val collection = tokens[index++]

        val conditions = mutableListOf<Condition>()
        var groupBy: String? = null
        var orderBy: OrderBy? = null
        var limit: Int? = null

        while (index < tokens.size) {
            when (tokens[index].uppercase()) {
                "WHERE" -> {
                    index++
                    conditions.addAll(parseConditions())
                }
                "GROUP" -> {
                    index++
                    expect("BY")
                    groupBy = tokens[index++]
                }
                "ORDER" -> {
                    index++
                    expect("BY")
                    val field = tokens[index++]
                    val desc = if (tokens.getOrNull(index)?.uppercase() == "DESC") {
                        index++; true
                    } else {
                        if (tokens.getOrNull(index)?.uppercase() == "ASC") index++
                        false
                    }
                    orderBy = OrderBy(field, desc)
                }
                "LIMIT" -> {
                    index++
                    limit = tokens[index++].toInt()
                }
                else -> error("Unexpected token '${tokens[index]}'")
            }
        }

        return Query(fields, collection, conditions, groupBy, orderBy, limit)
    }

    fun String.toComparable(example: Any?): Comparable<*> {
        return when (example) {
            is Int -> this.toInt()
            is Double -> this.toDouble()
            is Boolean -> this.toBoolean()
            else -> this
        }
    }

    fun evaluateCondition(doc: Map<String, Any>, cond: Condition): Boolean {
        val actual = doc[when (cond) {
            is Condition.Comparison -> cond.field
            is Condition.Between -> cond.field
        }]
        return when (cond) {
            is Condition.Comparison -> {
                when (cond.operator) {
                    "=" -> actual.toString() == cond.value
                    "!=" -> actual.toString() != cond.value
                    ">" -> ((actual as? Comparable<Any>)?.compareTo(cond.value.toComparable(actual)) ?: 0) > 0
                    "<" -> ((actual as? Comparable<Any>)?.compareTo(cond.value.toComparable(actual)) ?: 0) < 0
                    ">=" -> ((actual as? Comparable<Any>)?.compareTo(cond.value.toComparable(actual)) ?: 0) >= 0
                    "<=" -> ((actual as? Comparable<Any>)?.compareTo(cond.value.toComparable(actual)) ?: 0) <= 0
                    else -> error("Unknown operator ${cond.operator}")
                }
            }
            is Condition.Between -> {
                val low = cond.low.toComparable(actual)
                val high = cond.high.toComparable(actual)
                (actual as? Comparable<Any>)?.let { it >= low && it <= high } ?: false
            }
        }
    }

    fun evaluate(query: Query, db: Map<String, List<Map<String, Any>>>): List<MutableMap<String, Any?>> {
        if (query.groupBy != null) {
            val rows = db[query.collection] ?: error("Collection not found: ${query.collection}")
            val filtered = if (query.conditions.isNotEmpty()) {
                rows.filter { doc -> query.conditions.all { evaluateCondition(doc, it) } }
            } else rows

            val grouped = filtered.groupBy { it[query.groupBy] }

            val result = grouped.map { (groupValue, groupRows) ->
                val out = mutableMapOf<String, Any?>()
                out[query.groupBy] = groupValue

                for (sf in query.selectFields) {
                    when (sf) {
                        is SelectField.Aggregate -> {
                            val values = groupRows.mapNotNull { it[sf.field] as? Number }
                            val resultVal = when (sf.function) {
                                "SUM" -> values.sumOf { it.toDouble() }
                                "AVG" -> if (values.isNotEmpty()) values.sumOf { it.toDouble() } / values.size else 0.0
                                "MIN" -> values.minOfOrNull { it.toDouble() } ?: 0.0
                                "MAX" -> values.maxOfOrNull { it.toDouble() } ?: 0.0
                                "COUNT" -> values.size.toDouble()
                                else -> error("Unknown aggregate: ${sf.function}")
                            }
                            out[sf.alias] = resultVal
                        }
                        is SelectField.Expression -> {
                            val a = (groupRows.firstOrNull()?.get(sf.left) as? Number)?.toDouble() ?: 0.0
                            val b = (groupRows.firstOrNull()?.get(sf.right) as? Number)?.toDouble() ?: 1.0
                            val value = when (sf.op) {
                                "/" -> if (b != 0.0) a / b else 0.0
                                "*" -> a * b
                                "+" -> a + b
                                "-" -> a - b
                                else -> error("Unknown op ${sf.op}")
                            }
                            out[sf.alias] = value
                        }
                        is SelectField.Field -> {
                            out[sf.name] = groupRows.firstOrNull()?.get(sf.name)
                        }
                    }
                }

                out
            }

            val sorted = if (query.orderBy != null) {
                result.sortedBy { it[query.orderBy.field] as? Comparable<Any> }
                    .let { if (query.orderBy.descending) it.reversed() else it }
            } else result

            return if (query.limit != null) sorted.take(query.limit) else sorted
        }
        val rows = db[query.collection] ?: error("Collection not found: ${query.collection}")
        val filtered = if (query.conditions.isNotEmpty()) {
            rows.filter { doc -> query.conditions.all { evaluateCondition(doc, it) } }
        } else rows

        val result = filtered.map { doc ->
            val out = mutableMapOf<String, Any?>()
            for (sf in query.selectFields) {
                when (sf) {
                    is SelectField.Field -> out[sf.name] = doc[sf.name]
                    is SelectField.Expression -> {
                        val a = (doc[sf.left] as? Number)?.toDouble() ?: 0.0
                        val b = (doc[sf.right] as? Number)?.toDouble() ?: 1.0
                        val value = when (sf.op) {
                            "/" -> if (b != 0.0) a / b else 0.0
                            "*" -> a * b
                            "+" -> a + b
                            "-" -> a - b
                            else -> error("Unsupported op ${sf.op}")
                        }
                        out[sf.alias] = value
                    }
                    is SelectField.Aggregate -> error("Cannot use aggregates without GROUP BY")
                }
            }
            out
        }

        val sorted = if (query.orderBy != null) {
            result.sortedBy { it[query.orderBy.field] as? Comparable<Any> }
                .let { if (query.orderBy.descending) it.reversed() else it }
        } else result

        return if (query.limit != null) sorted.take(query.limit) else sorted
    }


}