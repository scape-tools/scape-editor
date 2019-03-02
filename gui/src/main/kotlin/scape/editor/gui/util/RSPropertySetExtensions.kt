package scape.editor.gui.util

import scape.editor.gui.plugin.extension.ConfigExtension
import java.lang.reflect.Modifier
import java.util.HashMap

fun ConfigExtension.Companion.RSPropertySet.mapInstanceFields(instance: Any): ConfigExtension.Companion.RSPropertySet {
    val map = HashMap<String, Any>()
    val set = ConfigExtension.Companion.RSPropertySet()
    set.properties = map

    try {
        val fields = instance.javaClass.declaredFields

        for (field in fields) {

            if (Modifier.isStatic(field.modifiers)) {
                continue
            }

            field.isAccessible = true

            val name = field.name

            val value = field.get(instance) ?: continue

            map[name] = value
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }

    return set
}