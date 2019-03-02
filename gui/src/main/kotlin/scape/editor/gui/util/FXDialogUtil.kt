package scape.editor.gui.util

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception

object FXDialogUtil {
    fun showException(ex: Exception) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Exception"
        alert.headerText = "Oops, there's an exception!"
        alert.contentText = "Something went wrong!"

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

    fun showError(msg: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.alertType = Alert.AlertType.ERROR
        alert.title = "Exception"
        alert.headerText = "Oops, there's an exception!"
        alert.contentText = msg

        Platform.runLater {
            alert.showAndWait()
        }
    }
}