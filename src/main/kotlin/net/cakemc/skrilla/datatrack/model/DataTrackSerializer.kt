package net.cakemc.skrilla.datatrack

import java.lang.reflect.Field

object DataTrackSerializer {

    fun serialize(obj: Any, adapter: ReadWriteAdapter) {
        val fields = obj.javaClass.declaredFields
        for (field in fields) {
            val annotation = field.getAnnotation(DataTracker::class.java) ?: continue
            field.isAccessible = true
            val key = annotation.documentName.ifEmpty { field.name }
            val value = field.get(obj)
            adapter.put(key, value)
        }
    }

    fun <T : Any> deserialize(clazz: Class<T>, adapter: ReadWriteAdapter): T {
        val constructor = clazz.declaredConstructors.firstOrNull()
            ?: throw IllegalArgumentException("No constructor found for ${clazz.name}")
        constructor.isAccessible = true

        // Prepare constructor params: For annotated fields, get value from adapter or default,
        // for non-annotated fields, use default values.
        val constructorParams = constructor.parameters.mapIndexed { index, param ->
            val field = clazz.declaredFields.getOrNull(index)
                ?: return@mapIndexed getDefaultValue(param.type)

            val annotation = field.getAnnotation(DataTracker::class.java)
            if (annotation != null) {
                val key = annotation.documentName.ifEmpty { field.name }
                val value = adapter.get(key)
                if (value == null && param.type.isPrimitive) {
                    getDefaultValue(param.type)
                } else {
                    value
                }
            } else {
                // No annotation -> default value
                getDefaultValue(param.type)
            }
        }.toTypedArray()

        val instance = constructor.newInstance(*constructorParams) as T

        // Set all fields (annotated or not), set annotated from adapter or default, else default
        for (field in clazz.declaredFields) {
            field.isAccessible = true
            val annotation = field.getAnnotation(DataTracker::class.java)
            val value = if (annotation != null) {
                val key = annotation.documentName.ifEmpty { field.name }
                adapter.get(key) ?: getDefaultValue(field.type)
            } else {
                // no annotation -> default value
                getDefaultValue(field.type)
            }

            // Set only if not null or if primitive
            if (value != null || field.type.isPrimitive) {
                field.set(instance, value)
            }
        }

        return instance
    }

    private fun getDefaultValue(type: Class<*>): Any? = when {
        type == java.lang.Boolean.TYPE || type == Boolean::class.java -> false
        type == java.lang.Byte.TYPE || type == Byte::class.java -> 0.toByte()
        type == java.lang.Short.TYPE || type == Short::class.java -> 0.toShort()
        type == java.lang.Integer.TYPE || type == Int::class.java -> 0
        type == java.lang.Long.TYPE || type == Long::class.java -> 0L
        type == java.lang.Float.TYPE || type == Float::class.java -> 0f
        type == java.lang.Double.TYPE || type == Double::class.java -> 0.0
        type == java.lang.Character.TYPE || type == Char::class.java -> '\u0000'
        type == String::class.java -> ""
        else -> null
    }
}
