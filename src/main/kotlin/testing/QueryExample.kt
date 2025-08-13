package testing

import net.cakemc.skrilla.query.QueryParser


fun main() {
    simpleGet()
    ageRestriction()
    dateGrouping()
    rationCalculation()
}

fun simpleGet() {
    val users = listOf(
        mapOf("name" to "Alice", "age" to 30, "logins" to 12),
        mapOf("name" to "Bob", "age" to 25, "logins" to 8),
        mapOf("name" to "Charlie", "age" to 30, "logins" to 20)
    )

    val db = mapOf("users" to users)

    val result = runQuery("SELECT name, logins FROM users WHERE age = 30 ORDER BY logins DESC LIMIT 1", db)
    println(result)

}

fun dateGrouping() {
    val orders = listOf(
        mapOf("order_date" to "2024-01-01", "order_amount" to 100),
        mapOf("order_date" to "2024-01-01", "order_amount" to 150),
        mapOf("order_date" to "2024-01-02", "order_amount" to 200)
    )
    val db = mapOf("orders" to orders)

    val result = runQuery(
        """
        SELECT order_date, SUM(order_amount)
        FROM orders
        GROUP BY order_date
        ORDER BY order_date ASC
        LIMIT 10
        """.trimIndent(), db
    )

    println(result)
}

fun ageRestriction() {
    val users = listOf(
        mapOf("name" to "Test", "age" to 10),
        mapOf("name" to "Alice", "age" to 25),
        mapOf("name" to "Bob", "age" to 30),
        mapOf("name" to "Charlie", "age" to 35),
        mapOf("name" to "David", "age" to 45)
    )

    val db = mapOf("users" to users)

    val query = """
    SELECT name, age FROM users 
    WHERE age BETWEEN 20 AND 40 
    ORDER BY age DESC 
    LIMIT 3
""".trimIndent()

    val result = QueryParser.evaluate(QueryParser.parse(QueryParser.tokenize(query)), db)

    println(result)

}

fun rationCalculation() {
    val players = listOf(
        mapOf("player_name" to "Alice", "KILLS" to 10, "DEATHS" to 2),
        mapOf("player_name" to "Bob", "KILLS" to 15, "DEATHS" to 5),
        mapOf("player_name" to "Charlie", "KILLS" to 2, "DEATHS" to 2),
        mapOf("player_name" to "Dave", "KILLS" to 8, "DEATHS" to 1),
        mapOf("player_name" to "Eve", "KILLS" to 5, "DEATHS" to 5)
    )

    val db = mapOf("players" to players)

    val result = runQuery(
        """
        SELECT player_name, KILLS / DEATHS AS KD
        FROM players
        ORDER BY KD DESC
        LIMIT 5
        """.trimIndent(), db
    )

    println(result)
}

fun runQuery(sql: String, db: Map<String, List<Map<String, Any>>>): List<MutableMap<String, Any?>> {
    val tokens = QueryParser.tokenize(sql)
    val query = QueryParser.parse(tokens)
    return QueryParser.evaluate(query, db)
}