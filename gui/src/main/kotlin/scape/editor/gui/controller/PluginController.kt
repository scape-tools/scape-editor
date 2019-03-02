package scape.editor.gui.controller

import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle
import scape.editor.gui.model.PluginModel
import scape.editor.gui.plugin.PluginDescriptor
import scape.editor.gui.plugin.PluginManager
import scape.editor.gui.plugin.IPlugin
import scape.editor.gui.util.FXDialogUtil
import java.lang.Exception
import java.net.URL
import java.util.*

class PluginController : BaseController() {

    @FXML
    lateinit var tableView : TableView<PluginModel>

    @FXML
    lateinit var nameCol : TableColumn<PluginModel, String>

    @FXML
    lateinit var versionCol : TableColumn<PluginModel, String>

    @FXML
    lateinit var searchTf : TextField

    private val data = FXCollections.observableArrayList<PluginModel>()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        nameCol.setCellValueFactory { it.value.nameProperty}
        versionCol.setCellValueFactory { it.value.versionProperty }


        val filteredList = FilteredList(data) { true}
        searchTf.textProperty().addListener { _, _, newValue -> filteredList.setPredicate {
            if (newValue == null || newValue.isEmpty()) {
                return@setPredicate true
            }

            val lowercase = newValue.toLowerCase()

            if (it.name.toLowerCase().contains(lowercase)) {
                return@setPredicate true
            }

            return@setPredicate false
        }
        }

        val sortedList = SortedList(filteredList)
        sortedList.comparatorProperty().bind(tableView.comparatorProperty())

        tableView.items = sortedList

        for (plugin in PluginManager.plugins) {
            val annotation = plugin.javaClass.getAnnotation(PluginDescriptor::class.java)
            val model = PluginModel(annotation.name, annotation.version, plugin)
            data.add(model)
        }
    }

    private fun open(instance: IPlugin) {
        try {
            val stylesheetPaths = mutableListOf<String>()

            val jarPath = PluginManager.jarNameMap[instance]

            if (jarPath == null) {
                FXDialogUtil.showError("Could not find plugin jar path.")
                return
            }

            for(cssPath in instance.stylesheets()) {
                val cssResource = buildResourcePath(jarPath, cssPath)
                stylesheetPaths.add(cssResource)
            }

            val fxmlResource = buildResourcePath(jarPath, instance.fxml())

            val loader = FXMLLoader(URL(fxmlResource))
            loader.classLoader = PluginManager.classLoaderMap[instance.hashCode()]
            val root = loader.load() as Parent
            val base = loader.getController() as BaseController
            base.currentPlugin = instance

            val stage = Stage()
            val scene = Scene(root)
            stage.scene = scene
            stage.isResizable = false
            stage.initStyle(StageStyle.UNDECORATED)
            stage.scene.stylesheets.addAll(stylesheetPaths)
            stage.icons.add(Image(URL(buildResourcePath(jarPath, instance.applicationIcon())).openStream()))

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
        val instance = selectedItem.instance

        if (instance is IPlugin) {
            open(instance)
        }
    }

}