package scape.editor.gui.model

import javafx.beans.property.SimpleStringProperty

class PluginModel(val name: String, val version: String, val instance: Any) {

    val nameProperty = SimpleStringProperty(name)

    val versionProperty = SimpleStringProperty(version)

    override fun toString(): String {
        return name
    }

}