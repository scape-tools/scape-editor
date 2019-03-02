package scape.editor.gui.model

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.image.ImageView
import scape.editor.gui.Settings

class StoreModel(val id: Int, val name: String) {

    val idProperty = SimpleIntegerProperty(id)
    val nameProperty = SimpleStringProperty(name)
    val icon = ImageView(Settings.getIcon("file_store_32.png"))

}