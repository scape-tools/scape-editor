package scape.editor.gui.controller

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.stage.DirectoryChooser
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import scape.editor.gui.App
import scape.editor.gui.Settings
import java.io.File

abstract class BaseController : Initializable {

    var currentPlugin = Any()

    var xOffset:Double = 0.toDouble()
    var yOffset:Double = 0.toDouble()

    @FXML
    private fun onMouseClicked(event: MouseEvent) {
        currentStage = (event.target as Node).scene.window as Stage
        xOffset = 0.0
        yOffset = 0.0
    }

    @FXML
    private fun minimizeProgram(e: ActionEvent) {
        val node = e.source as Node
        currentStage = node.scene.window as Stage
        currentStage.isIconified = true
    }

    fun openScene(sceneName: String) {
        val loader = FXMLLoader(App::class.java.getResource("/scenes/$sceneName.fxml"))
        val root = loader.load<Parent>()

        val stage = Stage()
        val scene = Scene(root)
        stage.scene = scene
        stage.isResizable = false
        stage.initStyle(StageStyle.UNDECORATED)
        stage.scene.stylesheets.add(App::class.java.getResource("/style.css").toExternalForm())
        stage.icons.add(Image(App::class.java.getResourceAsStream("/icons/icon.png")))
        stage.show()
        sceneCount++
    }

    fun switchScene(sceneName: String, instance: Any = Any()) {

        val loader = FXMLLoader(App::class.java.getResource("/scenes/$sceneName.fxml"))
        val root = loader.load<Parent>()
        val base = loader.getController() as BaseController
        base.currentPlugin = instance

        val stage = Stage()
        val scene = Scene(root)
        stage.scene = scene
        stage.isResizable = false
        stage.initStyle(StageStyle.UNDECORATED)
        stage.scene.stylesheets.add(App::class.java.getResource("/style.css").toExternalForm())
        stage.icons.add(Image(App::class.java.getResourceAsStream("/icons/icon.png")))

        val currentStage = BaseController.currentStage
        currentStage.isResizable = stage.isResizable
        currentStage.scene.stylesheets.clear()
        currentStage.scene.stylesheets.addAll(stage.scene.stylesheets)
        currentStage.icons.clear()
        currentStage.icons.addAll(stage.icons)
        currentStage.scene = stage.scene
        currentStage.centerOnScreen()
    }

    protected fun runTask(title: String, task: Task<*>) {
        val loader = FXMLLoader(App::class.java.getResource("/scenes/TaskScene.fxml"))
        val root = loader.load<Parent>()

        val controller = loader.getController<TaskController>()

        controller.title.text = title
        controller.createTask(task)

        val stage = Stage()
        val scene = Scene(root)
        stage.scene = scene
        stage.isResizable = false
        stage.initStyle(StageStyle.UNDECORATED)
        stage.scene.stylesheets.add(App::class.java.getResource("/task_style.css").toExternalForm())
        stage.icons.add(Image(App::class.java.getResourceAsStream("/icons/icon.png")))

        val screenWidth = Screen.getPrimary().visualBounds.width
        val screenHeight = Screen.getPrimary().visualBounds.height

        var x = Math.random() * screenWidth
        var y = Math.random() * screenHeight

        stage.show()
        sceneCount++

        if (x > (screenWidth - stage.width)) {
            x = screenWidth - stage.width
        }

        if (y > (screenHeight - stage.height)) {
            y = screenHeight - stage.height
        }

        stage.x = x
        stage.y = y
    }

    @FXML
    fun openFS() {
        if (App.fs.isLoaded) {
            onPopulate()
            return
        }

        val chooser = DirectoryChooser()
        chooser.title = "Select directory containing cache"
        chooser.initialDirectory = File("./")
        val selectedDir = chooser.showDialog(App.mainStage) ?: return
        App.fs.root = selectedDir.toPath()

        if (!App.fs.load()) {
            return
        }

        Settings.loadSettings()

        onPopulate()
    }

    open fun onPopulate() {

    }

    @FXML
    open fun closeProgram(e: ActionEvent) {
        if (e.source is Node) {
            val node = e.source as Node
            currentStage = node.scene.window as Stage

            if (sceneCount > 1) {
                currentStage.close()
            } else {
                Platform.exit()
            }
            sceneCount--
        } else {
            if (sceneCount > 1) {
                currentStage.close()
            } else {
                Platform.exit()
            }
        }
    }

    @FXML
    open fun handleMouseDragged(event: MouseEvent) {
        currentStage.x = event.screenX - xOffset
        currentStage.y = event.screenY - yOffset
    }
    @FXML
    open fun handleMousePressed(event:MouseEvent) {
        currentStage = (event.target as Node).scene.window as Stage
        xOffset = event.sceneX
        yOffset = event.sceneY
    }

    @FXML
    private fun clearProgram() {
        if (App.fs.isLoaded) {
            App.fs.reset()
        }
        onClear()
    }

    open fun onClear() {

    }

    companion object {
        var currentStage = App.mainStage
        var sceneCount = 1
    }

}