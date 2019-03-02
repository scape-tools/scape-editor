package scape.editor.gui.plugin.extension

import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import scape.editor.fs.RSArchive
import scape.editor.fs.RSFileStore
import scape.editor.fs.io.RSBuffer
import scape.editor.gui.App
import scape.editor.gui.model.KeyModel
import scape.editor.gui.plugin.PluginDescriptor
import scape.editor.gui.util.getFileNameWithoutExtension
import scape.editor.gui.util.mapInstanceFields
import scape.editor.gui.util.mapToInstance
import java.io.IOException

import java.util.*
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception


abstract class ConfigExtension: IPluginExtension {

    open abstract fun getFileName(): String

    open fun getStoreId(): Int {
        return RSFileStore.ARCHIVE_FILE_STORE
    }

    open fun getFileId(): Int {
        return RSArchive.CONFIG_ARCHIVE
    }

    open fun useMetaFile(): Boolean {
        return true
    }

    open fun readLength(buffer: RSBuffer): Int {
        return buffer.readUShort()
    }

    open fun writeLength(buffer: RSBuffer, size: Int) {
        buffer.writeShort(size)
    }

    open fun writeOffset(metaBuf: RSBuffer, dataBuf: RSBuffer, lastPos: Int) {
        metaBuf.writeShort(dataBuf.position - lastPos)
    }

    open fun setInitialDataBufOffset(buffer: RSBuffer) {
        buffer.position = 2
    }

    protected abstract fun decode(currentIndex: Int, buffer: RSBuffer)

    open fun onLoad(list: ObservableList<KeyModel>, archive: RSArchive) {
        try {
            val dataBuf = RSBuffer.wrap(archive.readFile(getDataFileName()).array())

            var length: Int

            if (useMetaFile()) {
                val metaBuf = RSBuffer.wrap(archive.readFile(getMetaFileName()).array())
                length = readLength(metaBuf)
                setInitialDataBufOffset(dataBuf)
            } else {
                length = readLength(dataBuf)
            }

            for (i in 0 until length) {
                val instance = this.javaClass.getConstructor().newInstance()
                instance.decode(i, dataBuf)
                val set = RSPropertySet().mapInstanceFields(instance)
                val model = KeyModel(i, set.getOrDefault("name", "null"), instance)
                model.map = TreeMap(set.properties)
                list.add(model)
            }

            onFinish(dataBuf)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    open fun showError(ex: Exception) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Error"
        alert.headerText = "Look, there's an error!"

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        val exceptionText = sw.toString()

        val label = Label("The exception stacktrace was:")

        val textArea = TextArea(exceptionText)
        textArea.isEditable = false
        textArea.isWrapText = true

        textArea.maxWidth = java.lang.Double.MAX_VALUE
        textArea.maxHeight = java.lang.Double.MAX_VALUE
        GridPane.setVgrow(textArea, Priority.ALWAYS)
        GridPane.setHgrow(textArea, Priority.ALWAYS)

        val expContent = GridPane()
        expContent.maxWidth = java.lang.Double.MAX_VALUE
        expContent.add(label, 0, 0)
        expContent.add(textArea, 0, 1)

        alert.dialogPane.expandableContent = expContent

        Platform.runLater {
            alert.showAndWait()
        }
    }

    open fun onSave(list: ObservableList<KeyModel>, archive: RSArchive) {
        try {
            if (list.isEmpty()) {
                return
            }

            val metaBuf = RSBuffer.init()
            val dataBuf = RSBuffer.init()

            writeLength(dataBuf, list.size)

            if (useMetaFile()) {
                writeLength(metaBuf, list.size)
            }

            for (i in 0 until list.size) {
                val item = list[i]
                val instance = item.instance as ConfigExtension
                item.map.mapToInstance(instance)

                var lastPos = dataBuf.position

                instance.encode(dataBuf)

                if (useMetaFile()) {
                    writeOffset(metaBuf, dataBuf, lastPos)
                }
            }

            archive.writeFile(getDataFileName(), dataBuf.toArray())

            if (useMetaFile()) {
                archive.writeFile(getMetaFileName(), metaBuf.toArray())
            }

            val store = App.fs.getStore(getStoreId()) ?: return
            val encoded = archive.encode() ?: return

            if (store.writeFile(getFileId(), encoded)) {
                val alert = Alert(Alert.AlertType.INFORMATION)
                alert.title = "Info"
                alert.headerText = "Success!"
                Platform.runLater { alert.show() }
            }

        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun onFinish(buffer: RSBuffer) {
        var name = this.javaClass.simpleName

        if (this.javaClass.isAnnotationPresent(PluginDescriptor::class.java)) {
            val plugin = this.javaClass.getAnnotation(PluginDescriptor::class.java)
            name = plugin.name
        }

        if (buffer.position != buffer.capacity()) {
            val alert = Alert(Alert.AlertType.WARNING)
            alert.title = "Warning"
            alert.headerText = String.format("%s was not fully loaded pos=%d capacity=%d", name, buffer.position, buffer.capacity())
            Platform.runLater { alert.show() }
        }

    }

    protected abstract fun encode(buffer: RSBuffer)

    open fun dataFileNameSuffix(): String {
        return DATA_SUFFIX
    }

    open fun metaFileNameSuffix(): String {
        return META_SUFFIX
    }

    private fun getDataFileName(): String {
        return "${getFileName().getFileNameWithoutExtension()}.${dataFileNameSuffix()}"
    }

    private fun getMetaFileName(): String {
        return "${getFileName().getFileNameWithoutExtension()}.${metaFileNameSuffix()}"
    }

    companion object {

        const val DATA_SUFFIX = "dat"
        const val META_SUFFIX = "idx"

        class RSPropertySet {

            var properties: Map<String, Any> = HashMap()

            fun <T : Any> getOrDefault(name: String, value: T): T {
                return (properties as java.util.Map<String, Any>).getOrDefault(name, value) as T
            }
        }

    }

}