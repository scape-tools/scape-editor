package scape.editor.gui.plugin

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PluginDescriptor(val name: String, val description: String = "", val authors: Array<String> = [], val version : String = "1.0.0")