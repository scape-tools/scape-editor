package scape.editor.gui.util

import java.lang.reflect.Modifier

fun MutableMap<String, Any>.mapToInstance(instance: Any) {
    try {
        val fields = instance.javaClass.declaredFields
        for (field in fields) {

            if (Modifier.isStatic(field.modifiers)) {
                continue
            }

            field.isAccessible = true

            val name = field.name

            if (containsKey(name)) {
                field.set(instance, get(name))
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}