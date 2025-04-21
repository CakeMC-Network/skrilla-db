package net.cakemc.database.filter

import net.cakemc.database.api.Document

/**
 * Represents a filter that can be applied to a [Document]. This is a functional interface
 * that extends the [Filter] interface with a specific type of [Document]. It allows for
 * filtering documents based on custom conditions.
 *
 * The implementing function defines the filtering logic for a [Document], typically returning
 * `true` for documents that match the condition and `false` for those that don't.
 *
 * Example:
 * ```kotlin
 * val filter: DocumentFilter = DocumentFilter { document ->
 *     document.getInt("age") > 18
 * }
 * ```
 *
 * @see Filter
 * @see Document
 */
fun interface DocumentFilter : Filter<Document>
