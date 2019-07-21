package scape.editor.gui.controller

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.concurrent.Task
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.StageStyle
import scape.editor.gui.App
import scape.editor.gui.model.PluginWrapper
import scape.editor.gui.plugin.PluginManager
import scape.editor.gui.util.FXDialogUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class PluginController : BaseController() {

    @FXML
    lateinit var tableView : TableView<PluginWrapper>

    @FXML
    lateinit var nameCol : TableColumn<PluginWrapper, String>

    @FXML
    lateinit var versionCol : TableColumn<PluginWrapper, String>

    @FXML
    lateinit var searchTf : TextField

    private val data = FXCollections.observableArrayList<PluginWrapper>()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        this.setupContextMenu()

        nameCol.setCellValueFactory { it.value.nameProperty}
        versionCol.setCellValueFactory { it.value.versionProperty }

        val filteredList = FilteredList(data) { true}
        searchTf.textProperty().addListener { _, _, newValue -> filteredList.setPredicate {
            if (newValue == null || newValue.isEmpty()) {
                return@setPredicate true
            }

            val lowercase = newValue.toLowerCase()

            if (it.meta.name.toLowerCase().contains(lowercase)) {
                return@setPredicate true
            }

            return@setPredicate false
        }
        }

        val sortedList = SortedList(filteredList)
        sortedList.comparatorProperty().bind(tableView.comparatorProperty())

        tableView.items = sortedList

        refreshList()
    }

    private fun setupContextMenu() {
        val menuItem1 = MenuItem("Add Plugin")
        menuItem1.setOnAction { this.addPlugin() }

        val menuItem2 = MenuItem("Remove Plugin")
        menuItem2.setOnAction { this.removePlugin() }

        val menuItem3 = MenuItem("Refresh")
        menuItem3.setOnAction { this.refreshList() }

        val contextMenu = ContextMenu(menuItem1, menuItem2, menuItem3)
        this.tableView.contextMenu = contextMenu
    }

    private fun refreshList() {
        data.clear()

        if (PluginManager.plugins.isNotEmpty()) {
            PluginManager.plugins.forEach {
                it.value.loader.close()
                PluginManager.unregister(it.value.plugin)
            }
            PluginManager.plugins.clear()
        }

        PluginManager.loadPlugins()
        for (plugin in PluginManager.plugins) {
            data.add(plugin.value)
        }
    }

    private fun open(value: PluginWrapper) {
        try {
            val stylesheetPaths = mutableListOf<String>()
            val jarPath = value.path



            for(cssPath in value.plugin.stylesheets()) {
                val cssResource = buildResourcePath(jarPath, cssPath)
                stylesheetPaths.add(cssResource)
            }

            val fxmlResource = buildResourcePath(jarPath, value.plugin.fxml())

            val loader = FXMLLoader(URL(fxmlResource))
            loader.classLoader = value.loader
            val root = loader.load() as Parent
            val base = loader.getController() as BaseController
            base.currentPlugin = value.plugin

            val stage = Stage()
            val scene = Scene(root)
            stage.scene = scene
            stage.isResizable = false
            stage.initStyle(StageStyle.UNDECORATED)
            stage.scene.stylesheets.addAll(stylesheetPaths)
            stage.icons.add(Image(URL(buildResourcePath(jarPath, value.plugin.applicationIcon())).openStream()))

            val currentStage = BaseController.currentStage
            currentStage.isResizable = stage.isResizable
            currentStage.scene.stylesheets.clear()
            currentStage.scene.stylesheets.addAll(stage.scene.stylesheets)
            currentStage.icons.clear()
            currentStage.icons.addAll(stage.icons)
            currentStage.scene = stage.scene
            currentStage.centerOnScreen()
        } catch (ex: Exception) {
            ex.printStackTrace()
            FXDialogUtil.showException(ex)
        }
    }

    private fun buildResourcePath(jarPath: String, resourcePath: String): String {
        val path = jarPath.replace("\\", "/")
        return "jar:file:$path!/$resourcePath"
    }

    @FXML
    fun open(event: ActionEvent) {
        val node = event.source as Node
        currentStage = node.scene.window as Stage

        val selectedItem = tableView.selectionModel.selectedItem ?: return
        open(selectedItem)
    }

    fun addPlugin() {
        val chooser = FileChooser()
        val filter = FileChooser.ExtensionFilter("Jar files (*.jar)", "*.jar")
        chooser.title = "Select your plugins to add"
        chooser.initialDirectory = File("./")
        chooser.extensionFilters.add(filter)

        val pluginPath = Paths.get(System.getProperty("user.home"), "scape-editor", "plugins")

        if (!Files.exists(pluginPath)) {
            if (!pluginPath.toFile().mkdirs()) {
                return
            }
        }

        val selectedFiles = chooser.showOpenMultipleDialog(App.mainStage) ?: return

        val task = object: Task<Boolean>() {
            override fun call(): Boolean {

                for(selectedFile in selectedFiles) {
                    val destPath = pluginPath.resolve(selectedFile.name).toFile()
                    FileInputStream(selectedFile).use { input ->
                        FileOutputStream(destPath).use { out ->
                            out.write(input.readAllBytes())
                        }
                    }
                }

                PluginManager.loadPlugins()

                Platform.runLater {
                    refreshList()
                }

                return true
            }

        }

        Thread(task).start()

    }

    fun removePlugin() {
        val selectedItem = this.tableView.selectionModel.selectedItem ?: return

        val path = Paths.get(selectedItem.path)

        if (Files.exists(path)) {
            try {
                val wrapper = PluginManager.plugins[selectedItem.path] ?: return
                wrapper.loader.close()
                PluginManager.unregister(wrapper.plugin)
                PluginManager.plugins.remove(selectedItem.path)
                Files.delete(path)
                this.refreshList()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }
    }

}