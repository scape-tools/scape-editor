package scape.editor.gui

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle
import scape.editor.fs.RSFileSystem
import scape.editor.gui.plugin.PluginManager

class App : Application() {

    override fun init() {
        Settings.load()
    }

    override fun start(stage: Stage) {
        mainStage = stage

        val root : Parent = FXMLLoader.load(App::class.java.getResource("/scenes/StoreScene.fxml"))
        stage.title = "Scape Editor [build $VERSION]"
        val scene = Scene(root)
        scene.stylesheets.add(App::class.java.getResource("/style.css").toExternalForm())
        stage.scene = scene
        stage.icons.add(Image(App::class.java.getResourceAsStream("/icons/icon.png")))
        stage.centerOnScreen()
        stage.isResizable = false
        stage.initStyle(StageStyle.UNDECORATED)
        stage.show()
    }

    override fun stop() {
        if (fs.isLoaded) {
            Settings.save(fs.root)
        }
    }

    companion object {
        val VERSION = "3.1.0"

        val fs = RSFileSystem()

        lateinit var mainStage : Stage

        @JvmStatic
        fun main(args : Array<String>) {
            launch(App::class.java)
        }
    }

}