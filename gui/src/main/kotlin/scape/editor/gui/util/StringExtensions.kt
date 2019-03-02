package scape.editor.gui.util

fun String.getFileNameWithoutExtension(): String {
    val pos = indexOf(".")

    if (pos != -1) {
        return substring(0, pos)
    }

    return this
}