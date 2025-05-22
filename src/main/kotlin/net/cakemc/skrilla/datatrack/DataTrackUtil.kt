package net.cakemc.skrilla.datatrack

object DataTrackUtil {

    fun toTrackedMap(obj: Any): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        val clazz = obj.javaClass

        for (field in clazz.declaredFields) {
            val annotation = field.getAnnotation(DataTracker::class.java) ?: continue
            field.isAccessible = true
            val key = if (annotation.documentName.isNotEmpty()) annotation.documentName else field.name
            result[key] = field.get(obj)
        }

        return result
    }

    fun <T : Any> fromTrackedMap(clazz: Class<T>, map: Map<String, Any?>): T {
        val constructor = clazz.declaredConstructors.firstOrNull()
            ?: throw IllegalArgumentException("No constructor found for class: ${clazz.name}")
        constructor.isAccessible = true

        val parameterValues = constructor.parameters.map { param ->
            // Try to find the matching field by position
            val name = param.name ?: "arg${constructor.parameters.indexOf(param)}"
            val field = clazz.declaredFields.getOrNull(constructor.parameters.indexOf(param))
                ?: throw IllegalArgumentException("Cannot resolve field for constructor parameter: $name")

            val annotation = field.getAnnotation(DataTracker::class.java)
            val key = annotation?.documentName?.takeIf { it.isNotBlank() } ?: field.name

            val value = map[key]
            if (!param.type.isPrimitive && value == null) {
                throw IllegalArgumentException("Missing required value for non-null field: $key")
            }

            // Handle primitive unboxing defaults
            if (value == null && param.type.isPrimitive) {
                when (param.type) {
                    java.lang.Boolean.TYPE -> false
                    java.lang.Byte.TYPE -> 0.toByte()
                    java.lang.Short.TYPE -> 0.toShort()
                    java.lang.Integer.TYPE -> 0
                    java.lang.Long.TYPE -> 0L
                    java.lang.Float.TYPE -> 0f
                    java.lang.Double.TYPE -> 0.0
                    java.lang.Character.TYPE -> '\u0000'
                    else -> throw IllegalArgumentException("Cannot handle default for primitive type: ${param.type}")
                }
            } else {
                value
            }
        }.toTypedArray()

        return constructor.newInstance(*parameterValues) as T
    }


}
