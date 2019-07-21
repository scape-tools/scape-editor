package scape.editor.gui.model

import javafx.beans.property.SimpleStringProperty
import scape.editor.gui.plugin.IPlugin
import scape.editor.gui.plugin.PluginClassLoader
import scape.editor.gui.plugin.PluginDescriptor

class PluginWrapper(val plugin: IPlugin, val loader: PluginClassLoader, val path: String, val meta: PluginDescriptor) {
    val nameProperty = SimpleStringProperty(meta.name)

    val versionProperty = SimpleStringProperty(meta.version)

    override fun toString(): String {
        return meta.name
    }
}