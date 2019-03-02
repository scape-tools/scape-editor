package scape.editor.gui.controller

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import java.net.URL
import java.util.*
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.concurrent.Task
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.ImageView
import scape.editor.gui.App
import scape.editor.gui.Settings
import scape.editor.gui.model.StoreEntryModel
import scape.editor.gui.model.StoreModel
import java.io.File
import java.io.FileOutputStream
import java.nio.channels.Channels
import java.util.zip.CRC32
import javafx.scene.control.Alert.AlertType
import javafx.scene.text.Text
import javafx.stage.*
import scape.editor.gui.event.LoadCacheEvent
import scape.editor.gui.plugin.PluginManager
import java.nio.file.Files
import kotlin.collections.HashSet


class MainController : BaseController() {

    @FXML
    lateinit var storeTable: TableView<StoreModel>

    @FXML
    lateinit var storeIconCol: TableColumn<StoreModel, ImageView>

    @FXML
    lateinit var storeNameCol: TableColumn<StoreModel, String>

    @FXML
    lateinit var storeIndexCol: TableColumn<StoreModel, Int>

    private val storeData = FXCollections.observableArrayList<StoreModel>()

    @FXML
    lateinit var storeEntryTable: TableView<StoreEntryModel>

    @FXML
    lateinit var storeEntryIconCol: TableColumn<StoreEntryModel, ImageView>

    @FXML
    lateinit var storeEntryFileCol: TableColumn<StoreEntryModel, Int>

    @FXML
    lateinit var storeEntryNameCol: TableColumn<StoreEntryModel, String>

    @FXML
    lateinit var storeEntrySizeCol: TableColumn<StoreEntryModel, String>

    @FXML
    lateinit var storeTf: TextField

    @FXML
    lateinit var storeEntryTf: TextField

    @FXML
    lateinit var title: Text

    private val storeEntryData = FXCollections.observableArrayList<StoreEntryModel>()

    override fun initialize(location: URL, resources: ResourceBundle?) {
        title.text = "Scape Editor [build ${App.VERSION}]"
        storeIconCol.cellValueFactory = PropertyValueFactory("icon")
        storeNameCol.setCellValueFactory { it.value.nameProperty }
        storeIndexCol.setCellValueFactory { it.value.idProperty as ObservableValue<Int> }

        val fileredStoreList = FilteredList(storeData) { true}
        storeTf.textProperty().addListener { _, _, newValue -> fileredStoreList.setPredicate {
            if (newValue == null || newValue.isEmpty()) {
                    return@setPredicate true
                }

                val lowercase = newValue.toLowerCase()

                if (it.name.toLowerCase().contains(lowercase) || it.id.toString().contains(lowercase)) {
                    return@setPredicate true
                }

                return@setPredicate false
            }
        }

        val sortedStoreList = SortedList(fileredStoreList)
        sortedStoreList.comparatorProperty().bind(storeTable.comparatorProperty())
        storeTable.items = sortedStoreList

        storeTable.selectionModel.selectedItemProperty().addListener { _, _ ,newValue ->

            newValue ?: return@addListener

            val id = newValue.id

            val task = object:Task<Boolean>() {
                override fun call(): Boolean {
                    val store = App.fs.getStore(id)

                    val files = store.fileCount

                    if (store == null || files <= 0) {
                        storeEntryData.clear()
                        return false
                    }

                    storeEntryData.clear()

                    for (i in 0 until files) {
                        val data = store.readFile(i)
                        val exists = data != null && data.capacity() > 0
                        val size = if (data == null ) 0 else data.capacity()
                        val gzipped = if (data == null) false else Settings.isGzip(data.array())

                        var storeEntryName = Settings.getStoreEntryReferenceName(id, i) ?: i.toString()

                        if (!storeEntryName.endsWith(".gz") && gzipped) {
                            if (storeEntryName.indexOf(".") != -1) {
                                storeEntryName = storeEntryName.substring(0, storeEntryName.indexOf("."))
                            }
                            storeEntryName = storeEntryName.plus(".gz")
                        }

                        val model = StoreEntryModel(i, storeEntryName, size)

                        if (exists) {
                            model.icon = ImageView(Settings.getIcon(data.array()))
                        } else {
                            model.icon = ImageView(Settings.getIcon("file_32.png"))
                        }

                        storeEntryData.add(model)
                    }

                    Platform.runLater {
                        storeEntryTable.refresh()
                    }

                    return true
                }

            }

            task.run()
        }

        storeEntryIconCol.cellValueFactory = PropertyValueFactory("icon")
        storeEntryFileCol.setCellValueFactory { it.value.idProperty as ObservableValue<Int> }
        storeEntryNameCol.setCellValueFactory { it.value.nameProperty }
        storeEntrySizeCol.setCellValueFactory { it.value.sizeProperty }

        val filteredStoreEntryList = FilteredList(storeEntryData) { true}
        storeEntryTf.textProperty().addListener { _, _, newValue -> filteredStoreEntryList.setPredicate {
            if (newValue == null || newValue.isEmpty()) {
                return@setPredicate true
            }

            val lowercase = newValue.toLowerCase()

            if (it.name.toLowerCase().contains(lowercase) || it.id.toString().contains(lowercase)) {
                return@setPredicate true
            }

            return@setPredicate false
        }
        }

        val sortedStoreEntryList = SortedList(filteredStoreEntryList)
        sortedStoreEntryList.comparatorProperty().bind(storeEntryTable.comparatorProperty())
        storeEntryTable.items = sortedStoreEntryList

        onPopulate()
    }

    @FXML
    private fun openPluginList() {
        openScene("PluginScene")
    }

    @FXML
    private fun deleteStore() {
        val selectedStore = storeTable.selectionModel.selectedItem ?: return
        val selectedIndex = storeTable.selectionModel.selectedIndex

        if (!App.fs.isLoaded) {
            return
        }

        val alert = Alert(AlertType.CONFIRMATION)
        alert.headerText = "Are you sure you want to delete this store?"

        val optional = alert.showAndWait()

        if (!optional.isPresent) {
            return
        }

        val result = optional.get()

        if (result != ButtonType.OK) {
            return
        }

        val task = object:Task<Boolean>() {
            override fun call(): Boolean {
                val path = App.fs.root
                App.fs.reset()
                Files.deleteIfExists(path.resolve("main_file_cache.idx${selectedStore.id}"))
                App.fs.load()
                App.fs.defragment()

                Platform.runLater {
                    storeData.removeAt(selectedIndex)
                }

                return true
            }

        }

        task.run()

    }

    @FXML
    private fun createStore() {
        val input = TextInputDialog()
        input.title = "Input"
        input.headerText = "Name this store"
        input.contentText = "Please enter a name:"
        val optional = input.showAndWait()

        if (!optional.isPresent) {
            return
        }

        val result = optional.get()

        val set = HashSet<Int>()

        var nextId = 0

        val sorted = storeData.sorted { o1, o2 ->  o1.id.compareTo(o2.id)}

        for (store in sorted) {
            set.add(store.id)

            if (set.contains(nextId)) {
                nextId++
            }
        }

        Settings.putStoreName(nextId, result)

        val task = object:Task<Boolean>() {
            override fun call(): Boolean {
                App.fs.createStore(nextId)

                Platform.runLater {
                    storeData.add(StoreModel(nextId, result))
                }
                return true
            }

        }

        task.run()

    }

    @FXML
    private fun importStoreEntries() {
        if (!App.fs.isLoaded) {
            return
        }

        val selectedStore = storeTable.selectionModel.selectedItem ?: return

        val chooser = DirectoryChooser()
        chooser.title = "Select directory to import"
        chooser.initialDirectory = File("./")
        val dir = chooser.showDialog(App.mainStage) ?: return
        val files = dir.listFiles() ?: return

        val task = object:Task<Boolean>() {
            override fun call(): Boolean {

                val store = App.fs.getStore(selectedStore.id) ?: return false

                for (i in 0 until files.size) {
                    val file = files[i] ?: continue
                    val data = Files.readAllBytes(file.toPath()) ?: continue
                    val gzipped = Settings.isGzip(data)

                    var id: Int

                    try {
                        var name = file.name

                        if (name.indexOf(".") != -1) {
                            name = name.substring(0, name.indexOf("."))
                        }

                        id = name.toInt()

                        if (id < 0) {
                            continue
                        }
                    } catch (ex: Exception) {
                        continue
                    }

                    store.writeFile(id, data)

                    var name = file.name

                    if (!name.endsWith(".gz") && gzipped) {
                        if (name.indexOf(".") != -1) {
                            name = name.substring(0, name.indexOf("."))
                        }
                        name = name.plus(".gz")
                    }

                    val model = StoreEntryModel(id, name, data.size)

                    if (gzipped) {
                        model.icon = ImageView(Settings.getIcon("gz_32.png"))
                    }

                    if (id < storeEntryData.size) {
                        storeEntryData[id] = model
                    } else {
                        storeEntryData.add(model)
                    }

                    val progress = (i + 1).toDouble() / files.size * 100
                    updateMessage(String.format("%.2f%s", progress, "%"))
                    updateProgress((i + 1).toDouble(), files.size.toDouble())

                }

                Platform.runLater {
                    storeEntryTable.refresh()
                }

                return true
            }
        }

        runTask("Import task", task)

    }

    @FXML
    private fun exportStoreEntries() {
        if (!App.fs.isLoaded) {
            return
        }

        val selectedStore = storeTable.selectionModel.selectedItem ?: return

        val chooser = DirectoryChooser()
        chooser.title = "Select a directory to export to"
        chooser.initialDirectory = File("./")
        val selectedDir = chooser.showDialog(App.mainStage) ?: return

        val indexDir = File(selectedDir, "index${selectedStore.id}")
        if (!indexDir.exists()) {
            indexDir.mkdirs()
        }

        val task = object:Task<Boolean>() {
            override fun call(): Boolean {
                val store = App.fs.getStore(selectedStore.id) ?: return false

                for (i in 0 until store.fileCount) {
                    val data= store.readFile(i) ?: continue

                    if (data.capacity() > 0) {
                        var name = Settings.getStoreEntryReferenceName(store.storeId, i)

                        if (name == null) {
                            if (Settings.isGzip(data.array())) {
                                name = "$i.gz"
                            } else {
                                name = "$i.dat"
                            }
                        }

                        FileOutputStream(File(indexDir, "$name")).use { fos ->
                            Channels.newChannel(fos).write(data)
                        }
                    }

                    val progress = (i + 1).toDouble() / store.fileCount * 100
                    updateMessage(String.format("%.2f%s", progress, "%"))
                    updateProgress((i + 1).toDouble(), store.fileCount.toDouble())

                }

                return true
            }
        }

        runTask("Export task", task)

    }

    @FXML
    private fun renameStore() {
        if (!App.fs.isLoaded) {
            return
        }

        val selectedStore = storeTable.selectionModel.selectedItem ?: return
        val selectedStoreIndex = storeTable.selectionModel.selectedIndex

        val dialog = TextInputDialog("walter")
        dialog.title = "Input"
        dialog.headerText = "Name a store"
        dialog.contentText = "Please enter a name:"
        val optional = dialog.showAndWait()

        if (!optional.isPresent) {
            return
        }

        val result = optional.get()

        Settings.putStoreName(selectedStore.id, result)
        storeData[selectedStoreIndex] = StoreModel(selectedStore.id, result)
        storeTable.refresh()

    }

    @FXML
    private fun addStoreEntry() {
        if (!App.fs.isLoaded) {
            return
        }

        val selectedStore = storeTable.selectionModel.selectedItem ?: return

        val chooser = FileChooser()
        chooser.title = "Select files to add"
        chooser.initialDirectory = File("./")
        val selectedFiles = chooser.showOpenMultipleDialog(App.mainStage) ?: return

        val task = object:Task<Boolean>() {
            override fun call(): Boolean {
                val store = App.fs.getStore(selectedStore.id) ?: return false

                for (i in 0 until selectedFiles.size) {
                    val selectedFile = selectedFiles[i]

                    val fileCount = store.fileCount
                    var id = fileCount

                    try {
                        var name = selectedFile.name

                        val pos = name.indexOf(".")

                        if (pos != -1) {
                            name = name.substring(0, name.indexOf("."))
                        }

                        id = name.toInt()
                    } catch (ex: Exception) {

                    }

                    val fileData = Files.readAllBytes(selectedFile.toPath())

                    if (store.writeFile(id, fileData)) {
                        val gzipped = Settings.isGzip(fileData)

                        var name = selectedFile.name

                        if (gzipped && !name.endsWith(".gz")) {
                            if (name.indexOf(".") != -1) {
                                name = name.substring(0, name.indexOf("."))
                            }
                            name = name.plus(".gz")
                        }

                        val model = StoreEntryModel(id, name, fileData.size)

                        if (gzipped) {
                            model.icon = ImageView(Settings.getIcon("gz_32.png"))
                        }

                        if (id < fileCount) {
                            storeEntryData[id] = model
                        } else {
                            storeEntryData.add(model)
                        }
                        Settings.putStoreEntryReferenceName(selectedStore.id, id, name)
                    }

                    val progress = (i + 1).toDouble() / selectedFiles.size * 100
                    updateMessage(String.format("%.2f%s", progress, "%"))
                    updateProgress((i + 1).toDouble(), selectedFiles.size.toDouble())

                }

                Platform.runLater {
                    storeEntryTable.refresh()
                }

                return true
            }
        }

        runTask("Import Task", task)
    }

    override fun onPopulate() {
        if (!App.fs.isLoaded) {
            return
        }

        PluginManager.post(LoadCacheEvent(App.fs))
        storeEntryData.clear()
        storeData.clear()

        for (i in 0 until App.fs.storeCount) {
            val storeName = Settings.getStoreReferenceName(i) ?: "unknown"
            val model = StoreModel(i, storeName)
            storeData.add(model)
        }
    }

    @FXML
    private fun exportStoreEntry() {
        if (!App.fs.isLoaded) {
            return
        }

        val selectedStore = storeTable.selectionModel.selectedItem ?: return
        val selectedStoreEntries = storeEntryTable.selectionModel.selectedItems ?: return

        val chooser = DirectoryChooser()
        chooser.title = "Select a directory to export to"
        chooser.initialDirectory = File("./")
        val selectedDir = chooser.showDialog(App.mainStage) ?: return

        val task = object:Task<Boolean>() {
            override fun call(): Boolean {
                val store = App.fs.getStore(selectedStore.id) ?: return false

                for (entry in selectedStoreEntries) {
                    val buf = store.readFile(entry.id) ?: continue

                    var name = entry.name

                    if (!entry.name.endsWith(".gz") && Settings.isGzip(buf.array())) {
                        if (name.indexOf(".") != -1) {
                            name = name.substring(0, name.indexOf("."))
                        }
                        name = name.plus(".gz")
                        Settings.putStoreEntryReferenceName(selectedStore.id, entry.id, name)
                    }

                    FileOutputStream(File(selectedDir, name)).use { fos ->
                        val channel = Channels.newChannel(fos)
                        channel.write(buf)
                    }

                }
                return true
            }
        }

        task.run()

    }

    @FXML
    private fun replaceStoreEntry() {
        if (!App.fs.isLoaded) {
            return
        }

        val selectedStore = storeTable.selectionModel.selectedItem ?: return
        val selectedStoreEntry = storeEntryTable.selectionModel.selectedItem ?: return

        val chooser = FileChooser()
        chooser.title = "Select file"
        chooser.initialDirectory = File("./")
        val selectedFile = chooser.showOpenDialog(App.mainStage) ?: return

        if (selectedFile.length() <= 0) {
            return
        }

        val task = object:Task<Boolean>() {
            override fun call(): Boolean {

                val store = App.fs.getStore(selectedStore.id) ?: return false

                val data= Files.readAllBytes(selectedFile.toPath())

                if (store.writeFile(selectedStoreEntry.id, data)) {
                    val model = StoreEntryModel(selectedStoreEntry.id, selectedFile.name, data.size)
                    model.icon = ImageView(Settings.getIcon(data))
                    storeEntryData[selectedStoreEntry.id] = model
                    Settings.putStoreEntryReferenceName(store.storeId, selectedStoreEntry.id, selectedFile.name)
                }

                val progress = 1.0 / 1 * 100
                updateMessage(String.format("%.2f%s", progress, "%"))
                updateProgress((2).toDouble(), 1.0)

                return true
            }
        }

        runTask("Replace Task", task)

    }

    override fun onClear() {
        storeEntryData.clear()
        storeData.clear()
    }

    @FXML
    private fun removeStoreEntry() {
        if (!App.fs.isLoaded) {
            return
        }

        val selectedStore = storeTable.selectionModel.selectedItem ?: return
        val selectedStoreEntries = storeEntryTable.selectionModel.selectedItems ?: return

        val alert = Alert(AlertType.CONFIRMATION)
        alert.headerText = "Are you sure you want to replace this file?"

        val optional = alert.showAndWait()

        if (!optional.isPresent) {
            return
        }

        val result = optional.get()

        if (result != ButtonType.OK) {
            return
        }

        val task = object:Task<Boolean>() {
            override fun call(): Boolean {

                val store = App.fs.getStore(selectedStore.id) ?: return false

                var flag = false

                for (selectedEntry in selectedStoreEntries) {
                    val file = selectedEntry.id

                    if (store.writeFile(file, ByteArray(0))) { // defrag uses len 0 to determine if a file store should be truncated
                        val entry = storeEntryData[file]
                        val model = StoreEntryModel(entry.id, entry.name, 0)
                        model.icon = ImageView(Settings.getIcon("file_32.png"))
                        storeEntryData[file] = model

                        if (file == store.fileCount - 1) {

                            // determine the number of files that need to be removed from the end of the store
                            var removeAmount = 0

                            for (i in storeEntryData.size - 1 downTo 0) {
                                if (storeEntryData[i].size <= 0) {
                                    removeAmount++
                                } else {
                                    break
                                }
                            }

                            for (i in 0 until removeAmount) {
                                Platform.runLater {
                                    storeEntryData.removeAt(storeEntryData.size - 1)
                                }
                            }

                            flag = true
                        }
                    }
                }

                if (flag) {
                    App.fs.defragment()
                }

                Platform.runLater {
                    storeEntryTable.refresh()
                }

                return true
            }
        }

        task.run()

    }

    @FXML
    private fun computeChecksum() {
        if (!App.fs.isLoaded) {
            return
        }

        val selectedStore = storeTable.selectionModel.selectedItem ?: return
        val selectedStoreEntry = storeEntryTable.selectionModel.selectedItem ?: return

        val crc = CRC32()

        val task = object:Task<Boolean>() {
            override fun call(): Boolean {
                val store = App.fs.getStore(selectedStore.id) ?: return false

                val buf = store.readFile(selectedStoreEntry.id)

                var checksum = 0L

                if (buf != null && buf.capacity() > 0) {
                    crc.update(buf.array())
                    checksum = crc.value
                }

                val alert = Alert(AlertType.INFORMATION)
                alert.title = "Computed checksum"
                alert.headerText = "$checksum"

                Platform.runLater {
                    alert.show()
                }

                return true
            }
        }

        task.run()

    }


}