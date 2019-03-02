package scape.editor.gui.model

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty

class KeyModel(val id: Int, val name: String = "null", val instance: Any){

    val idProperty = SimpleIntegerProperty(id)
    val nameProperty = SimpleStringProperty(name)

    var map = mutableMapOf<String, Any>()

    override fun toString() : String {
        return id.toString()
    }

}