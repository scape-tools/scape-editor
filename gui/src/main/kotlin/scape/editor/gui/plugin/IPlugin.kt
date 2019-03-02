package scape.editor.gui.plugin

interface IPlugin {

    fun fxml(): String

    fun stylesheets(): Array<String>

    fun applicationIcon(): String

}