package scape.editor.gui.model

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

class NamedValueModel(val name: String, val value: Any) {

    val nameProperty = SimpleStringProperty(name)
    val valueProperty = SimpleObjectProperty(ValueModel(KeyModel(-1, "", -1), name, value))

}